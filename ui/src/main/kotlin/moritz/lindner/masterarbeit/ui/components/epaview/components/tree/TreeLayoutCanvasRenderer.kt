package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.TimeRadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.computeBoundingBox
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.drawDepthCircles
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.toOffset
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DrawAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.highlight.HighlightingAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.labels.StateLabels
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.TabState
import moritz.lindner.masterarbeit.ui.logger
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TreeLayoutCanvasRenderer(
    treeLayout: TreeLayout,
    stateLabels: StateLabels,
    drawAtlas: DrawAtlas,
    onStateHover: (State?) -> Unit,
    onRightClick: (State?) -> Unit,
    onLeftClick: (State?) -> Unit,
    tabState: TabState,
    highlightingAtlas: HighlightingAtlas,
    animationState: AnimationState,
    canvasState: CanvasState,
) {
    var canvasSize by remember { mutableStateOf(IntSize.Companion.Zero) }

    var hoveredNode by remember(treeLayout) { mutableStateOf<NodePlacement?>(null) }
    var pressedNode by remember(treeLayout) { mutableStateOf<NodePlacement?>(null) }

    LaunchedEffect(tabState.locateState) {
        if (tabState.locateState != null) {
            val targetNode = treeLayout.getCoordinate(tabState.locateState)

            val screenCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            canvasState.offset = screenCenter - targetNode.toOffset() * canvasState.scale
        }
    }

    val canvasModifier = Modifier.Companion
        .background(Color.Companion.White)
        .onSizeChanged { canvasSize = it }
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

                    if (event.type == PointerEventType.Companion.Scroll && scrollDelta != 0f) {
                        val cursorPosition = event.changes.first().position

                        val zoomFactor = if (scrollDelta < 0) 1.1f else 0.9f
                        val newScale = (canvasState.scale * zoomFactor).coerceIn(0.01f, 14f)
                        val worldPosBefore =
                            TreeCanvasRenderingHelper.screenToWorld(
                                cursorPosition,
                                canvasState.offset,
                                canvasState.scale
                            )

                        canvasState.scale = newScale
                        canvasState.offset = cursorPosition - worldPosBefore * canvasState.scale
                    }
                }
            }
        }.pointerInput(treeLayout) {
            // Mouse hover detection
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()

                    if (event.type == PointerEventType.Companion.Move || event.type == PointerEventType.Companion.Enter || event.type == PointerEventType.Companion.Press) {
                        val screenPosition = event.changes.first().position

                        // Update hovered node if it changed
                        val newNode = TreeCanvasRenderingHelper.findNodeAt(
                            treeLayout,
                            TreeCanvasRenderingHelper.screenToWorld(
                                screenPosition,
                                canvasState.offset,
                                canvasState.scale
                            )
                        )

                        if (newNode != hoveredNode) {
                            hoveredNode = newNode
                            onStateHover(hoveredNode?.state)
                        }
                        if (
                            event.type == PointerEventType.Companion.Press &&
                            event.button == PointerButton.Companion.Primary &&
                            pressedNode != newNode
                        ) {
                            pressedNode = newNode
                            onRightClick(pressedNode?.state)
                        }
                        if (
                            event.type == PointerEventType.Companion.Press &&
                            event.button == PointerButton.Companion.Secondary &&
                            pressedNode != newNode
                        ) {
                            pressedNode = newNode
                            onLeftClick(pressedNode?.state)
                        }

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
                pivot = Offset.Companion.Zero,
            )
        }) {
            try {
                val visibleNodes = treeLayout.getCoordinatesInRectangle(
                    rectangle = computeBoundingBox(
                        canvasState.offset,
                        canvasState.scale
                    )
                )
                when (treeLayout) {
                    is RadialWalkerTreeLayout -> {
                        drawDepthCircles(layout = treeLayout)
                        drawTreeWithNodesAndEdges(
                            drawAtlas,
                            visibleNodes,
                            treeLayout,
                            highlightingAtlas,
                            tabState,
                            canvasState.scale,
                            stateLabels,
                            animationState
                        )
                    }

                    is DirectAngularPlacementTreeLayout -> {
                        drawDepthCircles(layout = treeLayout)
                        drawTreeWithNodesAndEdges(
                            drawAtlas,
                            visibleNodes,
                            treeLayout,
                            highlightingAtlas,
                            tabState,
                            canvasState.scale,
                            stateLabels,
                            animationState
                        )
                    }

                    is TimeRadialWalkerTreeLayout -> {
                        treeLayout.forEach { node ->
                            val radius = sqrt(node.coordinate.x.pow(2) + node.coordinate.y.pow(2))
                            drawCircle(
                                color = Color.Companion.Gray,
                                radius = radius,
                                center = Offset.Companion.Zero,
                                style = Stroke(width = 2f),
                                alpha = 0.1f
                            )
                        }

                        drawTreeWithNodesAndEdges(
                            drawAtlas,
                            visibleNodes,
                            treeLayout,
                            highlightingAtlas,
                            tabState,
                            canvasState.scale,
                            stateLabels,
                            animationState
                        )
                    }

                    is WalkerTreeLayout -> {
                        drawTreeWithNodesAndEdges(
                            drawAtlas,
                            visibleNodes,
                            treeLayout,
                            highlightingAtlas,
                            tabState,
                            canvasState.scale,
                            stateLabels,
                            animationState
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "error while drawing: $e" }
            }
        }
    }
}