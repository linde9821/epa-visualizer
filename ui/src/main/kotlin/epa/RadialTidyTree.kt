package epa

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import moritz.lindner.masterarbeit.drawing.Coordinate
import moritz.lindner.masterarbeit.drawing.layout.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State

@Composable
fun RadialTidyTree(
    epa: ExtendedPrefixAutomata<Long>,
    treeLayout: WalkerTreeLayout<Long>,
) {
    var zoom by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()

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
            val center = Offset(size.width / 2f, size.height / 2f)

//            (0..treeLayout.max).forEach { depth ->
//                drawCircle(
//                    color = Color.Gray,
//                    radius = depth * treeLayout.depthDistance,
//                    center = Offset(0f, 0.0f),
//                    style = Stroke(width = 2f), // Adjust the stroke width as needed
//                )
//            }

            drawCircle(
                color = Color.Red,
                radius = 5f,
                center = Offset(0f, 0.0f), // whatever you used in polarCoordinates()
            )

            epa.states
                .forEach { state ->
                    val coordinate = treeLayout.getCoordinate(state)

                    drawNode(state, textMeasurer, coordinate, center, epa)
                    when (state) {
                        is State.PrefixState -> {
                            val parentCoordinate = treeLayout.getCoordinate(state.from)

                            drawLine(
                                color = Color.Black,
                                start = Offset(parentCoordinate.x, parentCoordinate.y),
                                end = Offset(coordinate.x, coordinate.y),
                                strokeWidth = 5f,
                            )
                        }

                        State.Root -> {
                        }
                    }
                }
        }
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
        style = Stroke(width = 4f), // Adjust the stroke width as needed
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
