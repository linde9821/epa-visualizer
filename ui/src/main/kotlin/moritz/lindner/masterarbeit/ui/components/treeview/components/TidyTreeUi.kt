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
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.drawing.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.drawing.placement.Coordinate
import moritz.lindner.masterarbeit.epa.drawing.placement.Rectangle
import moritz.lindner.masterarbeit.ui.components.treeview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.treeview.state.UiState
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import org.jetbrains.skia.Color as SkiaColor

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

                    drawEPA(uiState.layout, boundingBox, animationState, scale)
                }
            }
        }
    }
}

private fun DrawScope.drawEPA(
    layout: TreeLayout,
    boundingBox: Rectangle,
    animationState: AnimationState,
    scale: Float,
) {
    val redFill =
        Paint().apply {
            color = SkiaColor.RED
            mode = PaintMode.FILL
            isAntiAlias = true
        }

    val blackFill =
        Paint().apply {
            color = SkiaColor.BLACK
            mode = PaintMode.FILL
            isAntiAlias = true
        }

    val redStroke =
        Paint().apply {
            color = SkiaColor.RED
            mode = PaintMode.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }

    val blackStroke =
        Paint().apply {
            color = SkiaColor.BLACK
            mode = PaintMode.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }

    val textPaint =
        Paint().apply {
            color = SkiaColor.BLACK
            isAntiAlias = true
        }
    val baseFontSize = 24f
    val skFont =
        org.jetbrains.skia
            .Font()
            .apply { size = baseFontSize }

    val result = layout.getCoordinatesInRectangle(boundingBox)

    drawIntoCanvas { canvas ->
        result.forEach { (coordinate, node) ->
            val state = node.state
            val isActive = animationState.contains(state)

            val circleRadius = if (isActive) 20f else 10f
            val fillPaint = if (isActive) redFill else blackFill

            val cx = coordinate.x
            val cy = -coordinate.y

            if (state is PrefixState) {
                val parentCoordinate = layout.getCoordinate(state.from)
                val start = Offset(parentCoordinate.x, -parentCoordinate.y)
                val end = Offset(cx, cy)
                val (c1, c2) = getControlPoints(parentCoordinate, coordinate, 0.5f)

                val path =
                    Path().apply {
                        moveTo(start.x, start.y)
                        cubicTo(c1.x, -c1.y, c2.x, -c2.y, end.x, end.y)
                    }

                val isAnimating =
                    animationState.current.any {
                        it.state == state.from &&
                            it.nextState == state &&
                            it.from <= animationState.time &&
                            animationState.time < (it.to ?: Long.MAX_VALUE)
                    }

                val edgePaint = if (isAnimating) redStroke else blackStroke
                canvas.nativeCanvas.drawPath(path, edgePaint)
            }

            canvas.nativeCanvas.drawCircle(cx, cy, circleRadius, fillPaint)

            val screenRadius = circleRadius * scale
            logger.info { screenRadius }

            if (screenRadius >= 27f) {
                val label = state.name // or state.name if available

                val textLine =
                    org.jetbrains.skia.TextLine
                        .make(label, skFont)

                canvas.nativeCanvas.drawTextLine(textLine, cx + circleRadius + 5f, cy + baseFontSize, textPaint)
            }
        }

        drawTokensWithSpreading(animationState, layout, canvas, redFill)
    }
}

private fun drawTokensWithSpreading(
    animationState: AnimationState,
    layout: TreeLayout,
    canvas: Canvas,
    redFill: Paint,
) {
    animationState.current.forEachIndexed { index, timedState ->
        val progress =
            if (timedState.to == null || timedState.nextState == null) {
                1f
            } else {
                val duration = timedState.to!! - timedState.from
                val elapsed = animationState.time - timedState.from
                (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            }

        val fromCoord = layout.getCoordinate(timedState.state)
        val toCoord = timedState.nextState?.let { layout.getCoordinate(it) }

        val tokenPosition =
            if (toCoord != null) {
                val (c1, c2) = getControlPoints(fromCoord, toCoord, 0.5f)
                interpolateBezier(
                    start = Offset(fromCoord.x, -fromCoord.y),
                    c1 = Offset(c1.x, -c1.y),
                    c2 = Offset(c2.x, -c2.y),
                    end = Offset(toCoord.x, -toCoord.y),
                    t = progress,
                )
            } else {
                Offset(fromCoord.x, -fromCoord.y)
            }

        // Spread tokens slightly if overlapping
        val angle = (index * (360f / animationState.current.size)) * (Math.PI / 180.0)
        val spread = 6f
        val dx = (spread * cos(angle)).toFloat()
        val dy = (spread * sin(angle)).toFloat()

        canvas.nativeCanvas.drawCircle(
            tokenPosition.x + dx,
            tokenPosition.y + dy,
            6f,
            redFill,
        )
    }
}

fun interpolateBezier(
    start: Offset,
    c1: Offset,
    c2: Offset,
    end: Offset,
    t: Float,
): Offset {
    val u = 1 - t
    return Offset(
        x =
            u.pow(3) * start.x +
                3 * u.pow(2) * t * c1.x +
                3 * u * t.pow(2) * c2.x +
                t.pow(3) * end.x,
        y =
            u.pow(3) * start.y +
                3 * u.pow(2) * t * c1.y +
                3 * u * t.pow(2) * c2.y +
                t.pow(3) * end.y,
    )
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

private fun Offset.toCoordinate(): Coordinate =
    Coordinate(
        x = this.x,
        y = this.y,
    )
