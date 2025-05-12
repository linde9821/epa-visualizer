package epa

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State

@Composable
fun RadialTidyTree(epa: ExtendedPrefixAutomata<Long>) {
    var zoom by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(Unit) {
//        offset = Offset(windowWidth / 2f, windowHeight / 2f)
    }
    val canvasModifier =
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    // Apply zoom, clamped to a reasonable range
                    zoom = (zoom * gestureZoom)
                    // Update offset (scroll position)
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
                            val mousePos = event.changes.first().position
                            val newZoom = (zoom * if (scrollDelta < 0) 1.1f else 0.9f).coerceIn(0.1f, 5f)

                            // Adjust the offset to keep the point under the mouse stationary
                            val scaleChange = newZoom / zoom
                            offset = (offset - mousePos) * scaleChange + mousePos

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
            val root: Set<Event<Long>> = epa.sequence(State.Root)
            drawNode(root, textMeasurer)
        }
    }
}

fun DrawScope.drawNode(
    events: Set<Event<Long>>,
    textMeasurer: TextMeasurer,
) {
    // Draw circle
    drawCircle(
        color = Color.Black,
        radius = 20f,
        center = center,
        style = Stroke(width = 4f), // Adjust the stroke width as needed
    )

    // Prepare the label
    val label = events.joinToString(", ")

    // Define text style
    val textStyle =
        TextStyle(
            fontSize = 8.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Red,
        )

    // Measure the text
    val textLayoutResult =
        textMeasurer.measure(
            text = AnnotatedString(label),
            style = textStyle,
        )

    // Calculate position to draw text next to the node
    val textPosition =
        Offset(
            x = center.x + 30f,
            y = center.y - textLayoutResult.size.height / 2,
        )

    // Draw the text
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = textPosition,
    )
}

@Composable
fun EpaView(
    epa: ExtendedPrefixAutomata<Long>,
    scope: CoroutineScope,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    Row {
        Button(
            onClick = { onClose() },
        ) {
            Text("Close")
        }
    }
    Row(modifier = Modifier.background(Color.White).fillMaxWidth()) {
        Column(
            modifier = Modifier.background(Color.Red).fillMaxWidth(0.2f).fillMaxHeight(),
        ) {
            Text("UI Component Filter")
        }
        Column(
            modifier = Modifier.background(Color.Blue).fillMaxSize(),
        ) {
            Row(modifier = Modifier.background(Color.White).fillMaxWidth()) {
                RadialTidyTree(epa)
            }
            Row(modifier = Modifier.background(Color.Yellow).fillMaxWidth()) {
                Text("UI Component Timeline")
            }
        }
    }
}
