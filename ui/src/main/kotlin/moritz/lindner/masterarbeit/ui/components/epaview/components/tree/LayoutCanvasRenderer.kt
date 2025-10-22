package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.getControlPoints
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.screenToWorld
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DrawAtlas
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LayoutCanvasRenderer(
    layout: Layout,
    canvasState: CanvasState,
    drawAtlas: DrawAtlas
) {
    val canvasModifier = Modifier
        .background(Color.White)
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTransformGestures { centroid, pan, zoom, _ ->
                canvasState.scale *= zoom
                canvasState.offset += (centroid - canvasState.offset) * (1f - zoom) + pan
            }
        }.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f

                    if (event.type == PointerEventType.Scroll && scrollDelta != 0f) {
                        val cursorPosition = event.changes.first().position

                        val zoomFactor = if (scrollDelta < 0) 1.1f else 0.9f
                        val newScale = (canvasState.scale * zoomFactor).coerceIn(0.01f, 14f)
                        val worldPosBefore = screenToWorld(cursorPosition, canvasState.offset, canvasState.scale)

                        canvasState.scale = newScale
                        canvasState.offset = cursorPosition - worldPosBefore * canvasState.scale
                    }
                }
            }
        }.clipToBounds()

    Canvas(modifier = canvasModifier) {
        withTransform({
            translate(canvasState.offset.x, canvasState.offset.y)
            scale(
                scaleX = canvasState.scale,
                scaleY = canvasState.scale,
                pivot = Offset.Zero,
            )
        }) {
            try {
                drawIntoCanvas { canvas ->
                    val path = Path()
                    layout.forEach { (coordinate, state) ->
                        if (state is PrefixState) {
                            val cx = coordinate.x
                            val cy = -coordinate.y
                            val parentCoordinate = layout.getCoordinate(state.from)
                            val entry = drawAtlas.getTransitionEntryByParentState(state)

                            val start = Offset(parentCoordinate.x, -parentCoordinate.y)
                            val end = Offset(cx, cy)
                            val (c1, c2) = getControlPoints(parentCoordinate, coordinate, 0.5f)

                            path.reset()
                            path.moveTo(start.x, start.y)
                            path.lineTo(end.x, end.y)

                            canvas.nativeCanvas.drawPath(path, entry.paint)
                        }
                    }


                    layout.forEach { (coordinate, state) ->
                        val entry = drawAtlas.getState(state)
                        val cx = coordinate.x
                        val cy = -coordinate.y

                        canvas.nativeCanvas.drawCircle(cx, cy, entry.size, entry.paint)
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "error while drawing: $e" }
            }
        }
    }
}

