package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.drawing.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.DirectAngularPlacement
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.placement.Coordinate
import moritz.lindner.masterarbeit.epa.drawing.placement.Rectangle
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import kotlin.math.PI
import org.jetbrains.skia.Color as SkiaColor

private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

val logger = KotlinLogging.logger {}

@Composable
fun TidyTreeUi(
    epa: ExtendedPrefixAutomata<Long>,
    tree: EPATreeNode<Long>,
    dispatcher: CoroutineDispatcher,
    radius: Float,
    margin: Float,
    layoutSelection: LayoutSelection,
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }

    val mutex = remember { Mutex() }

    var layout: TreeLayout<Long>? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(epa, radius, tree, margin, layoutSelection) {
        isLoading = true
        mutex.withLock {
            withContext(dispatcher) {
                layout =
                    if (layoutSelection.name == "Walker") {
                        WalkerTreeLayout(
                            distance = margin,
                            yDistance = radius,
                            expectedCapacity = epa.states.size,
                        )
                    } else if (layoutSelection.name == "Walker Radial Tree") {
                        RadialWalkerTreeLayout(
                            margin = margin.degreesToRadians(),
                            layerSpace = radius,
                            expectedCapacity = epa.states.size,
                        )
                    } else {
                        DirectAngularPlacement(
                            layerSpace = radius,
                            expectedCapacity = epa.states.size,
                        )
                    }

                layout!!.build(tree)
                isLoading = false
            }
        }
    }

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
                            val newScale = (oldScale * if (scrollDelta < 0) 1.1f else 0.9f).coerceIn(0.0005f, 10f)
                            scale = newScale

                            val worldPosAfter = worldPosBefore * scale
                            offset = cursorPosition - worldPosAfter
                        }
                    }
                }
            }.clipToBounds()

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
            val topLeft = Offset(x = center.x - ((size.width / scale) / 2f), y = center.y - ((size.height / scale) / 2f))
            val bottomRight = Offset(x = center.x + ((size.width / scale) / 2f), y = center.y + ((size.height / scale) / 2f))

            val boundingBox = Rectangle(topLeft.toCoordinate(), bottomRight.toCoordinate())
            if (!isLoading && layout != null && layout!!.isBuilt()) {
                (layout as? DirectAngularPlacement)?.let {
                    drawDepthCircles(it)
                }

                (layout as? RadialWalkerTreeLayout)?.let {
                    drawDepthCircles(it)
                }

                drawEPA(layout!!, boundingBox)
            }
        }
    }
}

private fun DrawScope.drawEPA(
    layout: TreeLayout<Long>,
    boundingBox: Rectangle,
) {
    val search = layout.getCoordinatesInRectangle(boundingBox)
    logger.info { "drawing ${search.size} nodes" }
    drawIntoCanvas { canvas ->
        search.forEach { (coordinate, node) ->
            val state = node.state
            val paint =
                Paint().apply {
                    color = SkiaColor.BLACK
                    mode = PaintMode.FILL
                    isAntiAlias = true
                }

            canvas.nativeCanvas.drawCircle(coordinate.x, -coordinate.y, 10f, paint)

            if (state is PrefixState) {
                val parentCoordinate = layout.getCoordinate(state.from)
                val start = Offset(parentCoordinate.x, -parentCoordinate.y)
                val end = Offset(coordinate.x, -coordinate.y)

                val (c1, c2) = getControlPoints(parentCoordinate, coordinate, 0.5f)

                val paint2 =
                    Paint().apply {
                        color = SkiaColor.BLACK
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

private fun DrawScope.drawDepthCircles(layout: RadialTreeLayout<Long>) {
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
