package moritz.lindner.masterarbeit.ui.components.treeview.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.drawing.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.drawing.placement.Coordinate
import moritz.lindner.masterarbeit.epa.drawing.placement.Rectangle
import moritz.lindner.masterarbeit.ui.components.treeview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.treeview.state.UiState
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import org.jetbrains.skia.Color as SkiaColor

val logger = KotlinLogging.logger {}

@Composable
fun TidyTreeUi(
    uiState: UiState,
    animationState: AnimationState,
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }

    val canvasModifier =
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    scale *= zoom
                    offset += (centroid - offset) * (1f - zoom) + pan
                }
            }.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val scrollDelta =
                            event.changes
                                .firstOrNull()
                                ?.scrollDelta
                                ?.y ?: 0f

                        if (event.type == PointerEventType.Scroll && scrollDelta != 0f) {
                            val cursorPosition = event.changes.first().position
                            val worldPosBefore = (cursorPosition - offset) / scale

                            val oldScale = scale
                            val newScale = (oldScale * if (scrollDelta < 0) 1.1f else 0.9f).coerceIn(0.01f, 14f)
                            scale = newScale

                            val worldPosAfter = worldPosBefore * scale
                            offset = cursorPosition - worldPosAfter
                        }
                    }
                }
            }.clipToBounds()

    if (uiState.isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            strokeWidth = 6.dp,
            color = MaterialTheme.colors.primary,
        )
    } else {
        Canvas(modifier = canvasModifier) {
            withTransform({
                translate(offset.x, offset.y)
                scale(
                    scaleX = scale,
                    scaleY = scale,
                    pivot = Offset.Zero,
                )
            }) {
                val center = (center - offset) / scale
                val topLeft =
                    Offset(x = center.x - ((size.width / scale) / 2f), y = center.y - ((size.height / scale) / 2f))
                val bottomRight =
                    Offset(x = center.x + ((size.width / scale) / 2f), y = center.y + ((size.height / scale) / 2f))

                val boundingBox = Rectangle(topLeft.toCoordinate(), bottomRight.toCoordinate())

                if (!uiState.isLoading && uiState.layout != null && uiState.layout.isBuilt()) {
                    (uiState.layout as? RadialTreeLayout)?.let {
                        drawDepthCircles(it)
                    }

                    drawEPA(uiState.layout, boundingBox, animationState)
                }
            }
        }
    }
}

private fun DrawScope.drawEPA(
    layout: TreeLayout,
    boundingBox: Rectangle,
    animationState: AnimationState,
) {
    val search = layout.getCoordinatesInRectangle(boundingBox)
//    logger.info { "drawing ${search.size} nodes" }
    drawIntoCanvas { canvas ->
        search.forEach { (coordinate, node) ->
            val state = node.state

            val col =
                if (animationState.current.contains(state)) {
                    SkiaColor.RED
                } else if (animationState.upComing.contains(state)) {
                    SkiaColor.GREEN
                } else if (animationState.previous.contains(state)) {
                    SkiaColor.MAGENTA
                } else {
                    SkiaColor.BLACK
                }

            val paint =
                Paint().apply {
                    color = col
                    mode = PaintMode.FILL
                    isAntiAlias = true
                }

            if (col == SkiaColor.RED) {
                canvas.nativeCanvas.drawCircle(coordinate.x, -coordinate.y, 20f, paint)
            }
            canvas.nativeCanvas.drawCircle(coordinate.x, -coordinate.y, 10f, paint)

            if (state is PrefixState) {
                val parentCoordinate = layout.getCoordinate(state.from)

                val start = Offset(parentCoordinate.x, -parentCoordinate.y)
                val end = Offset(coordinate.x, -coordinate.y)

                val (c1, c2) = getControlPoints(parentCoordinate, coordinate, 0.5f)

                val paint2 =
                    Paint().apply {
                        color = col
                        mode = PaintMode.STROKE
                        strokeWidth = 4f
                        isAntiAlias = true
                    }

                val path =
                    Path().apply {
                        moveTo(start.x, start.y)
                        cubicTo(c1.x, -c1.y, c2.x, -c2.y, end.x, end.y)
                    }

                canvas.nativeCanvas.drawPath(path, paint2)
            }
        }
    }
}

fun getControlPoints(
    parentCoordinate: Coordinate,
    coordinate: Coordinate,
    curvature: Float = 0.5f,
): Pair<Offset, Offset> {
    val dx = coordinate.x - parentCoordinate.x
    val dy = coordinate.y - parentCoordinate.y

    val c1 = Offset(parentCoordinate.x, parentCoordinate.y + dy * curvature)
    val c2 = Offset(coordinate.x, coordinate.y - dy * curvature)

    return Pair(c1, c2)
}

private fun DrawScope.drawDepthCircles(layout: RadialTreeLayout) {
    (0..layout.getMaxDepth()).forEach { depth ->
        drawCircle(
            color = Color.Gray,
            radius = depth * layout.getCircleRadius(),
            center = Offset.Zero,
            style = Stroke(width = 2f),
        )
    }
}

fun DrawScope.drawNodeLowLevel(coordinate: Coordinate) {
    drawIntoCanvas { canvas ->
        val paint =
            Paint().apply {
                color = SkiaColor.BLACK
                mode = PaintMode.FILL
                isAntiAlias = true
            }

        canvas.nativeCanvas.drawCircle(coordinate.x, -coordinate.y, 10f, paint)
    }
}

fun DrawScope.drawNode(coordinate: Coordinate) {
    drawCircle(
        color = Color.Black,
        radius = 10f,
        center = Offset(coordinate.x, coordinate.y * -1),
        style = Stroke(width = 4f),
    )
}

private fun Offset.toCoordinate(): Coordinate =
    Coordinate(
        x = this.x,
        y = this.y,
    )
