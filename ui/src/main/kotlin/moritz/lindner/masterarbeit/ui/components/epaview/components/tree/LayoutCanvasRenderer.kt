package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
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
import moritz.lindner.masterarbeit.epa.features.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.TimeRadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvas.computeBoundingBox
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvas.drawDepthCircles
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvas.drawTokensWithSpreading
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvas.findNodeAt
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvas.getControlPoints
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvas.screenToWorld
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvas.toOffset
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DrawAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.highlight.HighlightingAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.labels.StateLabels
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.TabState
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LayoutCanvasRenderer(
    treeLayout: TreeLayout,
    stateLabels: StateLabels,
    drawAtlas: DrawAtlas,
    onStateHover: (State?) -> Unit,
    onRightClick: (State?) -> Unit,
    onLeftClick: (State?) -> Unit,
    tabState: TabState,
    highlightingAtlas: HighlightingAtlas,
    animationState: AnimationState
) {
    var offset by remember() { mutableStateOf(Offset.Zero) }
    var scale by remember() { mutableFloatStateOf(1f) }
    var canvasSize by remember() { mutableStateOf(IntSize.Zero) }

    var hoveredNode by remember(treeLayout) { mutableStateOf<NodePlacement?>(null) }
    var pressedNode by remember(treeLayout) { mutableStateOf<NodePlacement?>(null) }

    LaunchedEffect(tabState.locateState) {
        if (tabState.locateState != null) {
            val targetNode = treeLayout.getCoordinate(tabState.locateState)

            val screenCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            offset = screenCenter - targetNode.toOffset() * scale
        }
    }

    val canvasModifier = Modifier
        .background(Color.White)
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

                    if (event.type == PointerEventType.Scroll && scrollDelta != 0f) {
                        val cursorPosition = event.changes.first().position

                        val zoomFactor = if (scrollDelta < 0) 1.1f else 0.9f
                        val newScale = (scale * zoomFactor).coerceIn(0.01f, 14f)
                        val worldPosBefore = screenToWorld(cursorPosition, offset, scale)

                        scale = newScale
                        offset = cursorPosition - worldPosBefore * scale
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
                        val newNode = findNodeAt(treeLayout, screenToWorld(screenPosition, offset, scale))

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
                            onRightClick(pressedNode?.state)
                        }
                        if (
                            event.type == PointerEventType.Press &&
                            event.button == PointerButton.Secondary &&
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
            translate(offset.x, offset.y)
            scale(
                scaleX = scale,
                scaleY = scale,
                pivot = Offset.Zero,
            )
        }) {
            try {
                val visibleNodes = treeLayout.getCoordinatesInRectangle(rectangle = computeBoundingBox(offset, scale))
                when(treeLayout) {
                    is RadialWalkerTreeLayout -> {
                        drawDepthCircles(layout = treeLayout)
                        drawTreeWithNodesAndEdges(
                            drawAtlas,
                            visibleNodes,
                            treeLayout,
                            highlightingAtlas,
                            tabState,
                            scale,
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
                            scale,
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

                        drawTreeWithNodesAndEdges(
                            drawAtlas,
                            visibleNodes,
                            treeLayout,
                            highlightingAtlas,
                            tabState,
                            scale,
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
                            scale,
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

private fun DrawScope.drawTreeWithNodesAndEdges(
    drawAtlas: DrawAtlas,
    visibleNodes: List<NodePlacement>,
    treeLayout: TreeLayout,
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
        visibleNodes.forEach { (coordinate, state) ->
            if (state is PrefixState) {
                val cx = coordinate.x
                val cy = -coordinate.y
                val parentCoordinate = treeLayout.getCoordinate(state.from)
                val entry = drawAtlas.getTransitionEntryByParentState(state)

                val start = Offset(parentCoordinate.x, -parentCoordinate.y)
                val end = Offset(cx, cy)
                val (c1, c2) = getControlPoints(parentCoordinate, coordinate, 0.5f)

                path.reset()
                path.moveTo(start.x, start.y)
                path.cubicTo(c1.x, -c1.y, c2.x, -c2.y, end.x, end.y)

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

        val selectedState = tabState.selectedState
        val selectedPaint = drawAtlas.selectedStatePaint
        val labelThreshold = drawAtlas.stateSizeUntilLabelIsDrawn

        // Draw nodes
        visibleNodes.forEach { (coordinate, state) ->
            val entry = drawAtlas.getState(state)
            val cx = coordinate.x
            val cy = -coordinate.y

            if (highlightingAtlas.highlightedStates.contains(state)) {
                canvas.nativeCanvas.drawCircle(cx, cy, entry.size + 15f, highlightedPaint)
            }

            canvas.nativeCanvas.drawCircle(cx, cy, entry.size, entry.paint)

            if (entry.size * scale >= labelThreshold) {
                val label = stateLabels.getLabelForState(state)
                canvas.nativeCanvas.drawImage(
                    label,
                    cx + entry.size + 5f,
                    cy - label.height / 2f,
                )
            }
        }

        // draw tokens
        try {
            drawTokensWithSpreading(
                animationState = animationState,
                visibleStates = visibleNodes.map(NodePlacement::state).toSet(),
                treeLayout = treeLayout,
                canvas = canvas,
                tokenPaint = drawAtlas.tokenPaint
            )
        } catch (e: Exception) {
            logger.error { e }
        }

        selectedState?.let {
            val coordinate = treeLayout.getCoordinate(selectedState)
            val cx = coordinate.x
            val cy = -coordinate.y
            val entry = drawAtlas.getState(selectedState)
            canvas.nativeCanvas.drawCircle(cx, cy, entry.size + 15f, selectedPaint)
        }
    }
}

object TreeCanvas {

    fun drawTokensWithSpreading(
        animationState: AnimationState,
        visibleStates: Set<State>,
        treeLayout: TreeLayout,
        canvas: Canvas,
        tokenPaint: Paint,
    ) {
        animationState
            .currentTimeStates
            .filter { timedState ->
                visibleStates.contains(timedState.state)
            }.forEachIndexed { index, timedState ->
                val progress =
                    if (timedState.endTime == null || timedState.nextState == null) {
                        1f
                    } else {
                        val duration = timedState.endTime!! - timedState.startTime
                        val elapsed = animationState.time - timedState.startTime
                        (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    }

                val fromCoord = treeLayout.getCoordinate(timedState.state)
                val toCoord = timedState.nextState?.let { treeLayout.getCoordinate(it) }

                val tokenPosition = if (toCoord != null) {
                    val (c1, c2) = getControlPoints(fromCoord, toCoord, 0.5f)
                    interpolateBezier(
                        start = Offset(fromCoord.x, -fromCoord.y),
                        c1 = Offset(c1.x, -c1.y),
                        c2 = Offset(c2.x, -c2.y),
                        end = Offset(toCoord.x, -toCoord.y),
                        t = progress,
                    )
                } else {
                    Offset(fromCoord.x, -fromCoord.y)
                }

                // Spread tokens slightly if overlapping
                val angle = (index * (360f / animationState.currentTimeStates.size)) * (Math.PI / 180.0)
                val spread = 9f
                val dx = (spread * cos(angle)).toFloat()
                val dy = (spread * sin(angle)).toFloat()

                canvas.nativeCanvas.drawCircle(
                    tokenPosition.x + dx,
                    tokenPosition.y + dy,
                    6f,
                    tokenPaint,
                )
            }
    }


    fun screenToWorld(screenPosition: Offset, offset: Offset, scale: Float): Offset =
        (screenPosition - offset) / scale

    fun findNodeAt(layout: TreeLayout, worldPos: Offset): NodePlacement? {
        val searchWidth = 10f
        return layout.getCoordinatesInRectangle(
            Rectangle(
                topLeft = Coordinate(worldPos.x - searchWidth, worldPos.y - searchWidth),
                bottomRight = Coordinate(worldPos.x + searchWidth, worldPos.y + searchWidth),
            )
        ).firstOrNull()
    }

    fun interpolateBezier(
        start: Offset,
        c1: Offset,
        c2: Offset,
        end: Offset,
        t: Float,
    ): Offset {
        val u = 1 - t
        return Offset(
            x =
                u.pow(3) * start.x +
                        3 * u.pow(2) * t * c1.x +
                        3 * u * t.pow(2) * c2.x +
                        t.pow(3) * end.x,
            y =
                u.pow(3) * start.y +
                        3 * u.pow(2) * t * c1.y +
                        3 * u * t.pow(2) * c2.y +
                        t.pow(3) * end.y,
        )
    }

    fun getControlPoints(
        parentCoordinate: Coordinate,
        coordinate: Coordinate,
        curvature: Float = 0.5f,
    ): Pair<Offset, Offset> {
        val dy = coordinate.y - parentCoordinate.y

        val c1 = Offset(parentCoordinate.x, parentCoordinate.y + dy * curvature)
        val c2 = Offset(coordinate.x, coordinate.y - dy * curvature)

        return Pair(c1, c2)
    }

    fun DrawScope.drawDepthCircles(layout: RadialTreeLayout) {
        (0..layout.getMaxDepth()).forEach { depth ->
            drawCircle(
                color = Color.Gray,
                radius = depth * layout.getCircleRadius(),
                center = Offset.Zero,
                style = Stroke(width = 2f),
            )
        }
    }

    fun Offset.toCoordinate(): Coordinate =
        Coordinate(
            x = this.x,
            y = this.y,
        )

    fun DrawScope.computeBoundingBox(
        offset: Offset,
        scale: Float
    ): Rectangle {
        val center = (center - offset) / scale
        val topLeft = Offset(
            x = center.x - ((size.width / scale) / 2f),
            y = center.y - ((size.height / scale) / 2f)
        )
        val bottomRight = Offset(
            x = center.x + ((size.width / scale) / 2f),
            y = center.y + ((size.height / scale) / 2f)
        )

        return Rectangle(topLeft.toCoordinate(), bottomRight.toCoordinate())
    }

    fun Coordinate.toOffset(): Offset {
        return Offset(
            x = this.x,
            y = this.y * -1
        )
    }
}