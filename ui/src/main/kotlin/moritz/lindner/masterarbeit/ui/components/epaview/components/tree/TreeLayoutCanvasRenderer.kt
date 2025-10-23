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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.PRTLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.TimeRadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.ClusteringLayout
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.computeBoundingBox
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.drawDepthCircles
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.drawNodes
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.drawTokensWithSpreading
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.getControlPoints
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.toOffset
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DrawAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.TransitionDrawMode
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.highlight.HighlightingAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.labels.StateLabels
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.TabState
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EpaLayoutCanvasRenderer(
    treeLayout: Layout,
    stateLabels: StateLabels,
    highlightingAtlas: HighlightingAtlas,
    animationState: AnimationState,
    drawAtlas: DrawAtlas,
    canvasState: CanvasState,
    tabState: TabState,
    onStateHover: (State?) -> Unit,
    onRightClickState: (State?) -> Unit,
    onLeftClickState: (State?) -> Unit,
//    onMultiSelect: (List<State>) -> Unit
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var hoveredNode by remember(treeLayout) { mutableStateOf<NodePlacement?>(null) }
    var pressedNode by remember(treeLayout) { mutableStateOf<NodePlacement?>(null) }

    LaunchedEffect(tabState.locateState) {
        if (tabState.locateState != null) {
            val targetNode = treeLayout.getCoordinate(tabState.locateState)

            val screenCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            canvasState.offset = screenCenter - targetNode.toOffset() * canvasState.scale
        }
    }

    val canvasModifier = Modifier
        .background(Color.White)
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

                    if (event.type == PointerEventType.Scroll && scrollDelta != 0f) {
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

                    if (event.type == PointerEventType.Move || event.type == PointerEventType.Enter || event.type == PointerEventType.Press) {
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
                            event.type == PointerEventType.Press &&
                            event.button == PointerButton.Primary &&
                            pressedNode != newNode
                        ) {
                            pressedNode = newNode
                            onRightClickState(pressedNode?.state)
                        }
                        if (
                            event.type == PointerEventType.Press &&
                            event.button == PointerButton.Secondary &&
                            pressedNode != newNode
                        ) {
                            pressedNode = newNode
                            onLeftClickState(pressedNode?.state)
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
                pivot = Offset.Zero,
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
                        drawTree(
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
                        drawTree(
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
                                color = Color.Gray,
                                radius = radius,
                                center = Offset.Zero,
                                style = Stroke(width = 2f),
                                alpha = 0.1f
                            )
                        }
                        drawTree(
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
                        drawTree(
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

                    is ClusteringLayout -> {
                        drawTree(
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

                    is PRTLayout -> {
                        drawTree(
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

fun DrawScope.drawTree(
    drawAtlas: DrawAtlas,
    visibleNodes: List<NodePlacement>,
    layout: Layout,
    highlightingAtlas: HighlightingAtlas,
    tabState: TabState,
    scale: Float,
    stateLabels: StateLabels,
    animationState: AnimationState
) {
    drawIntoCanvas { canvas ->

        val path = Path()
        val highlightedPaint = drawAtlas.highlightedPaint

        // Draw edges
        if (drawAtlas.getTransitionModeForLayout(layout) != TransitionDrawMode.NONE) {
            visibleNodes.forEach { (coordinate, state) ->
                if (state is PrefixState) {
                    val cx = coordinate.x
                    val cy = -coordinate.y
                    val parentCoordinate = layout.getCoordinate(state.from)
                    val entry = drawAtlas.getTransitionEntryByParentState(state)

                    val start = Offset(parentCoordinate.x, -parentCoordinate.y)
                    val end = Offset(cx, cy)

                    path.reset()
                    path.moveTo(start.x, start.y)
                    if (drawAtlas.getTransitionModeForLayout(layout) == TransitionDrawMode.BEZIER) {
                        val (c1, c2) = getControlPoints(parentCoordinate, coordinate, 0.5f)
                        path.cubicTo(c1.x, -c1.y, c2.x, -c2.y, end.x, end.y)
                    } else if (drawAtlas.getTransitionModeForLayout(layout) == TransitionDrawMode.LINE) {
                        path.lineTo(end.x, end.y)
                    }

                    if (highlightingAtlas.highlightedStates.contains(state)) {
                        val strokePaint = highlightedPaint.apply {
                            mode = PaintMode.STROKE
                            strokeWidth = entry.paint.strokeWidth + 5f
                        }
                        canvas.nativeCanvas.drawPath(path, strokePaint)
                    }

                    canvas.nativeCanvas.drawPath(path, entry.paint)
                }
            }
        }

        val selectedState = tabState.selectedState
        val selectedPaint = drawAtlas.selectedStatePaint

        // Draw nodes
        drawNodes(
            visibleNodes,
            drawAtlas,
            highlightingAtlas,
            canvas,
            scale,
            stateLabels
        )

        // draw tokens
        try {
            drawTokensWithSpreading(
                animationState = animationState,
                visibleStates = visibleNodes.map(NodePlacement::state).toSet(),
                layout = layout,
                canvas = canvas,
                tokenPaint = drawAtlas.tokenPaint
            )
        } catch (e: Exception) {
            logger.error(e) { "Error drawing tokens" }
        }

        selectedState?.let {
            val coordinate = layout.getCoordinate(selectedState)
            val cx = coordinate.x
            val cy = -coordinate.y
            val entry = drawAtlas.getState(selectedState)
            canvas.nativeCanvas.drawCircle(cx, cy, entry.size + 15f, selectedPaint)
        }
    }
}
