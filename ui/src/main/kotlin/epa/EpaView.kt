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
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.withContext
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitorProgressBar
import moritz.lindner.masterarbeit.epa.visitor.TreeBuildingVisitor
import moritz.lindner.masterarbeit.treelayout.Coordinate
import moritz.lindner.masterarbeit.treelayout.TreeLayout

@Composable
fun RadialTidyTree(
    epa: ExtendedPrefixAutomata<Long>,
    scope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    windowWidth: Int,
    windowHeight: Int,
) {
    var zoom by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()
    var treeLayout: TreeLayout<Long>? by remember { mutableStateOf(null) }
    var treeReady by remember { mutableStateOf(false) }

    LaunchedEffect(epa) {
        offset = Offset(windowWidth / 2f, windowHeight / 2f)
        withContext(dispatcher) {
            treeLayout = null
            val treeBuildingVisitor = TreeBuildingVisitor<Long>()
            epa.acceptDepthFirst(AutomataVisitorProgressBar(treeBuildingVisitor, "tree"))
            treeLayout = TreeLayout(treeBuildingVisitor.root, 70.0f)
            treeLayout!!.build()
            treeReady = true
        }
    }

    if (!treeReady) {
        CircularProgressIndicator()
    } else {
        val canvasModifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        // Apply zoom
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
                                val newZoom = (zoom * if (scrollDelta < 0) 1.1f else 0.9f).coerceIn(0.001f, 8f)

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
                epa.states.forEach { state ->
                    val coordinate = treeLayout!!.getCoordinates(state)
                    drawNode(state, textMeasurer, coordinate)
                    when (state) {
                        is State.PrefixState -> {
                            val parentCoordinate = treeLayout!!.getCoordinates(state.from)

                            drawLine(
                                color = Color.Black,
                                start = Offset(parentCoordinate.x, parentCoordinate.y * 120),
                                end = Offset(coordinate.x, coordinate.y * 120),
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
}

fun DrawScope.drawNode(
    node: State,
    textMeasurer: TextMeasurer,
    coordinate: Coordinate,
) {
    // Draw circle
    drawCircle(
        color = Color.Black,
        radius = 10f,
        center = Offset(coordinate.x.toFloat(), coordinate.y.toFloat() * 120),
        style = Stroke(width = 4f), // Adjust the stroke width as needed
    )

//    // Prepare the label
//    val label = events.joinToString(", ")
//
//    // Define text style
//    val textStyle =
//        TextStyle(
//            fontSize = 8.sp,
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
//            x = center.x + 30f,
//            y = center.y - textLayoutResult.size.height / 2,
//        )
//
//    // Draw the text
//    drawText(
//        textLayoutResult = textLayoutResult,
//        topLeft = textPosition,
//    )
}

@Composable
fun EpaView(
    epa: ExtendedPrefixAutomata<Long>,
    scope: CoroutineScope,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    windowWidth: Int,
    windowHeight: Int,
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
                RadialTidyTree(epa, scope, backgroundDispatcher, windowWidth, windowHeight)
            }
            Row(modifier = Modifier.background(Color.Yellow).fillMaxWidth()) {
                Text("UI Component Timeline")
            }
        }
    }
}
