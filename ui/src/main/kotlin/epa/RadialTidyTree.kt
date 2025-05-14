package epa

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.drawscope.scale
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
import moritz.lindner.masterarbeit.drawing.layout.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.domain.State.Root
import kotlin.math.PI

private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

@Composable
fun RadialTidyTree(
    epa: ExtendedPrefixAutomata<Long>,
    tree: EPATreeNode<Long>,
    dispatcher: CoroutineDispatcher,
    scope: CoroutineScope,
    value: Float,
    margin: Float,
) {
    val logger = KotlinLogging.logger {}

    val mutex by remember { mutableStateOf(Mutex()) }
    var zoom by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()
    var layout by remember { mutableStateOf<RadialWalkerTreeLayout<Long>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(epa, value, tree, margin) {
        isLoading = true
        mutex.withLock {
            withContext(dispatcher) {
                logger.info { "margine: $margin deg" }
                layout =
                    RadialWalkerTreeLayout(
                        depthDistance = value,
                        expectedCapacity = epa.states.size,
                        margin = margin.degreesToRadians(),
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
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom)
                    offset += pan
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
                            val newZoom = (zoom * if (scrollDelta < 0) 1.1f else 0.9f).coerceIn(0.0005f, 10f)

                            val scaleChange = newZoom / zoom
                            offset *= scaleChange

                            zoom = newZoom
                        }
                    }
                }
            }.clipToBounds()

    Canvas(modifier = canvasModifier) {
        withTransform({
            translate(offset.x, offset.y)
            scale(zoom)
        }) {
            if (!isLoading) {
                drawDepthCircles(layout!!)
                val center = Offset(size.width / 2f, size.height / 2f)
                drawEPA(epa, layout!!, textMeasurer, center)
            }
        }
    }
}

private fun DrawScope.drawEPA(
    epa: ExtendedPrefixAutomata<Long>,
    layout: RadialWalkerTreeLayout<Long>,
    textMeasurer: TextMeasurer,
    center: Offset,
) {
    epa.states.forEach { state ->
        drawState(layout, state, textMeasurer, center, epa)
    }
}

private fun DrawScope.drawState(
    layout: RadialWalkerTreeLayout<Long>,
    state: State,
    textMeasurer: TextMeasurer,
    center: Offset,
    epa: ExtendedPrefixAutomata<Long>,
) {
    val coordinate = layout.getCoordinate(state)
    drawNode(state, textMeasurer, coordinate, center, epa)
    drawEdge(state, layout, coordinate)
}

private fun DrawScope.drawEdge(
    state: State,
    layout: RadialWalkerTreeLayout<Long>,
    coordinate: Coordinate,
) {
    when (state) {
        is PrefixState -> {
            val parentCoordinate = layout.getCoordinate(state.from)
            val start = Offset(parentCoordinate.x, parentCoordinate.y)
            val end = Offset(coordinate.x, coordinate.y)

            val (c1, c2) = getControlPoints(parentCoordinate, coordinate, 0.5f)

            val path =
                Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(
                        c1.x,
                        c1.y,
                        c2.x,
                        c2.y,
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

private fun DrawScope.drawDepthCircles(layout: RadialWalkerTreeLayout<Long>) {
    (0..layout.maxDepth).forEach { depth ->
        drawCircle(
            color = Color.Gray,
            radius = depth * layout!!.depthDistance,
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
        center = Offset(coordinate.x, coordinate.y),
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
