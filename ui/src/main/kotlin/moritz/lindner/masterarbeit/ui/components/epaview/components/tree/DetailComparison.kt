package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.computeBoundingBox
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DrawAtlas

@Composable
fun DetailComparison(
    treeLayout: TreeLayout,
    drawAtlas: DrawAtlas,
) {
    var offset by remember { mutableStateOf(Offset.Companion.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var canvasSize by remember { mutableStateOf(IntSize.Companion.Zero) }

    val canvasModifier = Modifier.Companion
        .background(Color.Companion.White)
        .onSizeChanged { canvasSize = it }
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
                    val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f

                    if (event.type == PointerEventType.Companion.Scroll && scrollDelta != 0f) {
                        val cursorPosition = event.changes.first().position

                        val zoomFactor = if (scrollDelta < 0) 1.1f else 0.9f
                        val newScale = (scale * zoomFactor).coerceIn(0.01f, 14f)
                        val worldPosBefore = TreeCanvasRenderingHelper.screenToWorld(cursorPosition, offset, scale)

                        scale = newScale
                        offset = cursorPosition - worldPosBefore * scale
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
                pivot = Offset.Companion.Zero,
            )
        }) {
            drawIntoCanvas { canvas ->
                val visibleNodes = treeLayout.getCoordinatesInRectangle(rectangle = computeBoundingBox(offset, scale))

                visibleNodes.forEach { (coordinate, state) ->
                    val entry = drawAtlas.getState(state)
                    val cx = coordinate.x
                    val cy = coordinate.y

                    canvas.nativeCanvas.drawCircle(cx, cy, entry.size, entry.paint)
                }
            }
        }
    }
}