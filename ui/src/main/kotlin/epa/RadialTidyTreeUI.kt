package epa

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import moritz.lindner.masterarbeit.drawing.Coordinate
import moritz.lindner.masterarbeit.drawing.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.drawing.layout.Rectangle
import moritz.lindner.masterarbeit.drawing.layout.implementations.SimpleTreeLayout
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.domain.State.Root
import kotlin.math.PI

private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

val logger = KotlinLogging.logger {}

@Composable
fun RadialTidyTreeUI(
    epa: ExtendedPrefixAutomata<Long>,
    tree: EPATreeNode<Long>,
    dispatcher: CoroutineDispatcher,
    scope: CoroutineScope,
    value: Float,
    margin: Float,
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }

    val mutex = remember { Mutex() }

    val textMeasurer = rememberTextMeasurer()
    var layout by remember { mutableStateOf<SimpleTreeLayout<Long>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(epa, value, tree, margin) {
        isLoading = true
        mutex.withLock {
            withContext(dispatcher) {
                logger.info { "margine: $margin deg" }
                layout =
                    SimpleTreeLayout(
                        layerSpace = value,
//                        expectedCapacity = epa.states.size,
//                        margin = margin.degreesToRadians(),
                    )
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
                            val newZoom = (scale * if (scrollDelta < 0) 1.1f else 0.9f).coerceIn(0.0005f, 10f)

                            val scaleChange = newZoom / scale
                            offset *= scaleChange

                            scale = newZoom
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
            val t = (center - offset) / scale

            val topLeft = Offset(x = t.x - ((size.width / scale) / 2f), y = t.y - ((size.height / scale) / 2f))
            val bottomRight = Offset(x = t.x + ((size.width / scale) / 2f), y = t.y + ((size.height / scale) / 2f))

            val boundingBox = Rectangle(topLeft.toCoordinate(), bottomRight.toCoordinate())

            drawCircle(
                color = Color.Red,
                center = t,
                radius = 5.0f,
            )

            drawCircle(
                color = Color.Green,
                center = topLeft,
                radius = 50.0f,
            )

            drawCircle(
                color = Color.Blue,
                center = bottomRight,
                radius = 50.0f,
            )

            drawLine(
                color = Color.Blue,
                start = Offset(x = topLeft.x, y = topLeft.y),
                end = Offset(x = topLeft.x, y = topLeft.y),
            )

            logger.info { "center: $t" }
            logger.info { "top left: $topLeft" }
            logger.info { "bottom right: $bottomRight" }
            logger.info { "boundingBox: $boundingBox" }

            if (!isLoading && layout != null && layout!!.isBuilt()) {
                drawDepthCircles(layout!!)
                val center = Offset(size.width / 2f, size.height / 2f)
                drawEPA(epa, layout!!, textMeasurer, center, boundingBox)
            }
        }
    }
}

private fun Offset.toCoordinate(): Coordinate =
    Coordinate(
        x = this.x,
        y = this.y,
    )

private fun DrawScope.drawEPA(
    epa: ExtendedPrefixAutomata<Long>,
    layout: RadialTreeLayout<Long>,
    textMeasurer: TextMeasurer,
    center: Offset,
    boundingBox: Rectangle,
) {
    val search = layout.search(boundingBox)
    logger.info { "drawing ${search.size} nodes" }
    search.forEach {
        drawState(layout, it.node.state, textMeasurer, center, epa, it.coordinate)
    }
}

private fun DrawScope.drawState(
    layout: RadialTreeLayout<Long>,
    state: State,
    textMeasurer: TextMeasurer,
    center: Offset,
    epa: ExtendedPrefixAutomata<Long>,
    coordinate: Coordinate,
) {
    drawNode(state, textMeasurer, coordinate, center, epa)
    drawEdge(state, layout, coordinate)
}

private fun DrawScope.drawEdge(
    state: State,
    layout: RadialTreeLayout<Long>,
    coordinate: Coordinate,
) {
    when (state) {
        is PrefixState -> {
            val parentCoordinate = layout.getCoordinate(state.from)
            val start = Offset(parentCoordinate.x, parentCoordinate.y * -1)
            val end = Offset(coordinate.x, coordinate.y * -1)

            val (c1, c2) = getControlPoints(parentCoordinate, coordinate, 0.5f)

            val path =
                Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(
                        c1.x,
                        c1.y * -1,
                        c2.x,
                        c2.y * -1,
                        end.x,
                        end.y,
                    )
                }

            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 4f),
            )
        }

        Root -> {}
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
            center = Offset(0f, 0.0f),
            style = Stroke(width = 2f), // Adjust the stroke width as needed
        )
    }
}

fun DrawScope.drawNode(
    node: State,
    textMeasurer: TextMeasurer,
    coordinate: Coordinate,
    center: Offset,
    epa: ExtendedPrefixAutomata<Long>,
) {
    // Draw circle
    drawCircle(
        color = Color.Black,
        radius = 10f,
        center = Offset(coordinate.x, coordinate.y * -1),
//        style = Stroke(width = 4f), // Adjust the stroke width as needed
    )

//    // Prepare the label
//    val label = epa.sequence(node).joinToString { it.activity.name }
//
//    // Define text style
//    val textStyle =
//        TextStyle(
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Normal,
//            color = Color.Red,
//        )
//
//    // Measure the text
//    val textLayoutResult =
//        textMeasurer.measure(
//            text = AnnotatedString(label),
//            style = textStyle,
//        )
//
//    // Calculate position to draw text next to the node
//    val textPosition =
//        Offset(
//            x = coordinate.x + 30f,
//            y = coordinate.y - textLayoutResult.size.height / 2,
//        )
//
//    // Draw the text
//    drawText(
//        textLayoutResult = textLayoutResult,
//        topLeft = textPosition,
//    )
}
