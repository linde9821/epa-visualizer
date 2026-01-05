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
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.features.layout.ClusterLayout
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.factory.TransitionDrawMode
import moritz.lindner.masterarbeit.epa.features.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.PartitionClusteringLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.StateClusteringLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.parallelreadabletree.ParallelReadableTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.semantic.AngleSimilarityDepthTimeRadialLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.semantic.CycleTimeRadialLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.semantic.PartitionSimilarityRadialLayout
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.lod.LODQuery
import moritz.lindner.masterarbeit.epa.features.lod.NoLOD
import moritz.lindner.masterarbeit.epa.features.lod.steiner.SteinerTreeLOD
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.computeBoundingBox
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.drawDepthCircles
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.drawNodes
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.drawTokensWithSpreading
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.getControlPoint
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvasRenderingHelper.toOffset
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DrawAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.highlight.HighlightingAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.labels.StateLabels
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.TabState
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.PaintStrokeJoin
import org.jetbrains.skia.Path
import org.jetbrains.skia.PathEffect
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.time.measureTime

const val minScale = 0.05f
const val maxScale = 5f

private data class HeatmapData(
    val bitmap: Bitmap,
    val bounds: Rect
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EpaLayoutCanvasRenderer(
    layout: Layout,
    stateLabels: StateLabels,
    highlightingAtlas: HighlightingAtlas,
    lodQuery: LODQuery = NoLOD(),
    animationState: AnimationState,
    drawAtlas: DrawAtlas,
    canvasState: CanvasState,
    tabState: TabState,
    backgroundDispatcher: CoroutineDispatcher,
    onStateHover: (State?) -> Unit,
    onRightClickState: (State?) -> Unit,
    onLeftClickState: (State?) -> Unit,
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var hoveredNode by remember(layout) { mutableStateOf<NodePlacement?>(null) }
    var pressedNode by remember(layout) { mutableStateOf<NodePlacement?>(null) }

    LaunchedEffect(tabState.locateState) {
        if (tabState.locateState != null) {
            val targetNode = layout.getCoordinate(tabState.locateState)
            val screenCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            canvasState.offset = screenCenter - targetNode.toOffset() * canvasState.scale
        }
    }

    var heatmapBitmap by remember { mutableStateOf<HeatmapData?>(null) }

    // Calculate heatmap in background when layout changes
    LaunchedEffect(layout, drawAtlas) {
        when (layout) {
            is AngleSimilarityDepthTimeRadialLayout -> {
                if (layout.config.generateHeatmap) {
                    withContext(backgroundDispatcher) {
                        heatmapBitmap = null
                        val bitmap = calculateHeatmapBitmap(
                            layout,
                            drawAtlas,
                            blockSize = 40
                        )
                        heatmapBitmap = bitmap
                    }
                }
            }
        }
    }

    val dashLineLength = 100f
    val dashGap = 40f
    var dashPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis {
                dashPhase = (dashPhase + 1f) % (dashLineLength + dashGap)
            }
        }
    }

    LaunchedEffect(canvasState.scale) {
        if (lodQuery is SteinerTreeLOD<*>) {
            lodQuery.setLODFromZoom(canvasState.scale)
        }
    }

    val canvasModifier = Modifier
        .background(Color.LightGray)
        .onSizeChanged { size -> canvasSize = size }
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

                        val zoomFactor = if (scrollDelta < 0) 1.03f else 0.97f
                        val newScale = (canvasState.scale * zoomFactor).coerceIn(minScale, maxScale)
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
        }.pointerInput(layout) {
            // Mouse hover detection
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()

                    if (event.type == PointerEventType.Move || event.type == PointerEventType.Enter || event.type == PointerEventType.Press) {
                        val screenPosition = event.changes.first().position

                        // Update hovered node if it changed
                        val newNode = TreeCanvasRenderingHelper.findNodeAt(
                            layout,
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
            canvasState.initializeCenter(size)

            val time = measureTime {
                try {
                    val rectangle = computeBoundingBox(
                        canvasState.offset,
                        canvasState.scale
                    )

                    val visibleNodes = layout
                        .getCoordinatesInRectangle(rectangle)
                        .filter { node -> lodQuery.isVisible(node.state) }

                    when (layout) {
                        is RadialWalkerTreeLayout -> {
                            drawDepthCircles(layout = layout)
                            drawTree(
                                drawAtlas,
                                visibleNodes,
                                layout,
                                highlightingAtlas,
                                tabState,
                                canvasState.scale,
                                stateLabels,
                                animationState
                            )
                        }

                        is PartitionSimilarityRadialLayout -> {
                            drawDepthCircles(layout = layout)
                            drawTree(
                                drawAtlas,
                                visibleNodes,
                                layout,
                                highlightingAtlas,
                                tabState,
                                canvasState.scale,
                                stateLabels,
                                animationState
                            )
                        }

                        is DirectAngularPlacementTreeLayout -> {
                            drawDepthCircles(layout = layout)
                            drawTree(
                                drawAtlas,
                                visibleNodes,
                                layout,
                                highlightingAtlas,
                                tabState,
                                canvasState.scale,
                                stateLabels,
                                animationState
                            )
                        }

                        is CycleTimeRadialLayout, is AngleSimilarityDepthTimeRadialLayout -> {
                            heatmapBitmap?.let { (bitmap, bounds) ->
                                drawImage(
                                    image = bitmap.asComposeImageBitmap(),
                                    dstOffset = IntOffset(bounds.left.toInt(), bounds.top.toInt()),
                                    dstSize = IntSize(
                                        (bounds.right - bounds.left).toInt(),
                                        (bounds.bottom - bounds.top).toInt()
                                    ),
                                    alpha = 0.85f,
                                    blendMode = BlendMode.ColorBurn,
                                    filterQuality = FilterQuality.High
                                )
                            }

                            drawTimeCircles(layout)
                            drawTree(
                                drawAtlas,
                                visibleNodes,
                                layout,
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
                                layout,
                                highlightingAtlas,
                                tabState,
                                canvasState.scale,
                                stateLabels,
                                animationState
                            )
                        }

                        is StateClusteringLayout, is PartitionClusteringLayout -> {
                            drawTree(
                                drawAtlas,
                                visibleNodes,
                                layout,
                                highlightingAtlas,
                                tabState,
                                canvasState.scale,
                                stateLabels,
                                animationState
                            )

                            drawClusterOutline(dashLineLength, dashGap, dashPhase, layout)
                        }

                        is ParallelReadableTreeLayout -> {
                            drawTree(
                                drawAtlas,
                                visibleNodes,
                                layout,
                                highlightingAtlas,
                                tabState,
                                canvasState.scale,
                                stateLabels,
                                animationState
                            )
                        }

                        else -> throw IllegalStateException("Expected layout to be known")
                    }
                } catch (e: Exception) {
                    logger.error(e) { "error while drawing: $e" }
                }
            }

            if (time.inWholeMilliseconds > 50) {
                logger.warn { "rendering time is slow: ${time.inWholeMilliseconds}ms" }
            }
        }

        drawZoomLine(canvasState)
    }
}

private fun calculateHeatmapBitmap(
    treeLayout: Layout,
    drawAtlas: DrawAtlas,
    blockSize: Int,
): HeatmapData {
    val minXCoord = treeLayout.minOf { it.coordinate.x } - 450f
    val maxXCoord = treeLayout.maxOf { it.coordinate.x } + 450f
    val minYCoord = treeLayout.minOf { it.coordinate.y } - 450f
    val maxYCoord = treeLayout.maxOf { it.coordinate.y } + 450f

    val minX = (minXCoord / blockSize).roundToInt() * blockSize
    val minY = (minYCoord / blockSize).roundToInt() * blockSize
    val maxX = ((maxXCoord / blockSize) + 1).roundToInt() * blockSize
    val maxY = ((maxYCoord / blockSize) + 1).roundToInt() * blockSize

    val width = ((maxX - minX).toFloat() / blockSize).roundToInt()
    val height = ((maxY - minY).toFloat() / blockSize).roundToInt()

    val bitmap = Bitmap()
    bitmap.allocN32Pixels(width, height)
    val skiaCanvas = org.jetbrains.skia.Canvas(bitmap)

    var bitmapY = 0
    var worldY = minY.toFloat()
    while (worldY < maxY) {
        var bitmapX = 0
        var worldX = minX.toFloat()
        while (worldX < maxX) {
            val paint = calculateIDWColor(
                worldX + blockSize / 2f,
                worldY + blockSize / 2f,
                treeLayout,
                drawAtlas,
                power = 10f,
                maxDistance = 3000f,
            )

            skiaCanvas.drawRect(
                org.jetbrains.skia.Rect.makeXYWH(
                    bitmapX.toFloat(),
                    bitmapY.toFloat(),
                    1f,
                    1f
                ),
                paint
            )

            worldX += blockSize
            bitmapX++
        }
        worldY += blockSize
        bitmapY++
    }

    return HeatmapData(
        bitmap = bitmap,
        bounds = Rect(
            minX.toFloat(),
            minY.toFloat(),
            maxX.toFloat(),
            maxY.toFloat()
        )
    )
}

fun calculateIDWColor(
    x: Float,
    y: Float,
    points: Layout,
    drawAtlas: DrawAtlas,
    power: Float = 4f,
    maxDistance: Float = 200f,
): Paint {
    var weightedRed = 0f
    var weightedGreen = 0f
    var weightedBlue = 0f
    var weightedAlpha = 0f
    var totalWeight = 0f

    for (placement in points) {
        val paint = drawAtlas.getState(placement.state).paint
        val point = placement.coordinate
        val dx = x - point.x
        val dy = y - point.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance < 0.01f) {
            return paint
        }

        if (distance > maxDistance) continue

        val weight = 1f / distance.pow(power)

        // Extract ARGB components from Skia color
        val color = paint.color
        val a = ((color shr 24) and 0xFF) / 255f
        val r = ((color shr 16) and 0xFF) / 255f
        val g = ((color shr 8) and 0xFF) / 255f
        val b = (color and 0xFF) / 255f

        weightedRed += r * weight
        weightedGreen += g * weight
        weightedBlue += b * weight
        weightedAlpha += a * weight
        totalWeight += weight
    }

    return if (totalWeight > 0) {
        Paint().apply {
            color = org.jetbrains.skia.Color.makeARGB(
                (weightedAlpha / totalWeight * 255).toInt(),
                (weightedRed / totalWeight * 255).toInt(),
                (weightedGreen / totalWeight * 255).toInt(),
                (weightedBlue / totalWeight * 255).toInt()
            )
        }
    } else {
        Paint().apply {
            color = org.jetbrains.skia.Color.TRANSPARENT
        }
    }
}

private fun DrawScope.drawTimeCircles(treeLayout: Layout) {
    treeLayout
        .map { node -> sqrt(node.coordinate.x.pow(2) + node.coordinate.y.pow(2)) }
        .distinct()
        .forEach { radius ->
            drawCircle(
                color = Color.Gray,
                radius = radius,
                center = Offset.Zero,
                style = Stroke(width = 2f),
                alpha = 0.35f
            )
        }
}

private fun DrawScope.drawClusterOutline(
    dashLineLength: Float,
    dashGap: Float,
    dashPhase: Float,
    treeLayout: ClusterLayout
) {
    val pathPaint = Paint().apply {
        color = org.jetbrains.skia.Color.RED
        mode = PaintMode.STROKE
        isAntiAlias = true
        strokeWidth = 5f
        strokeCap = PaintStrokeCap.ROUND
        strokeJoin = PaintStrokeJoin.ROUND
        pathEffect = PathEffect.makeDash(
            floatArrayOf(dashLineLength, dashGap),
            dashPhase
        )
    }

    drawIntoCanvas { canvas ->
        treeLayout.getClusterPolygons().forEach { (_, coords) ->
            val path = Path().apply {
                moveTo(
                    coords.first().x,
                    coords.first().y
                )
                coords.drop(1).forEach { coord ->
                    lineTo(
                        coord.x,
                        coord.y
                    )
                }

                closePath()
            }

            canvas.nativeCanvas.drawPath(path, pathPaint)
        }
    }
}

private fun DrawScope.drawZoomLine(canvasState: CanvasState) {
    val padding = 20.dp.toPx()
    val lineLength = 180.dp.toPx()
    val lineHeight = 7.dp.toPx()
    val dotRadius = 8.dp.toPx()

    val normalized = ((ln(canvasState.scale) - ln(minScale)) / (ln(maxScale) - ln(minScale)))
        .coerceIn(0f, 1f)

    val topRightX = drawContext.size.width - padding

    val lineStart = Offset(topRightX - lineLength, padding)
    val lineEnd = Offset(topRightX, padding)
    drawLine(
        color = Color.Gray,
        start = lineStart,
        end = lineEnd,
        strokeWidth = lineHeight,
        cap = StrokeCap.Round
    )

    val color = Color(0xFF2196F3)

    val dotX = lineStart.x + normalized * (lineEnd.x - lineStart.x)
    drawCircle(
        color = color,
        radius = dotRadius,
        center = Offset(dotX, padding)
    )
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
        val highlightedIncomingPath = drawAtlas.pathFromRootPaint
        val highlightedOutgoingPath = drawAtlas.outgoingPathsPaint
        val transitionModeForLayout = drawAtlas.transitionDrawMode

        // Draw edges
        if (transitionModeForLayout != TransitionDrawMode.NONE) {
            visibleNodes
                .forEach { (childCoordinate, state) ->
                    if (state is PrefixState) {
                        val cx = childCoordinate.x
                        val cy = childCoordinate.y
                        val parentCoordinate = layout.getCoordinate(state.from)
                        val entry = drawAtlas.getTransitionEntryByParentState(state)

                        val start = Offset(parentCoordinate.x, parentCoordinate.y)
                        val end = Offset(cx, cy)

                        path.reset()
                        path.moveTo(start.x, start.y)

                        if (transitionModeForLayout == TransitionDrawMode.QUADRATIC_BEZIER) {
                            val controlPoint = getControlPoint(parentCoordinate, childCoordinate, .25f)
                            path.quadTo(controlPoint.x, controlPoint.y, end.x, end.y)
                        } else if (transitionModeForLayout == TransitionDrawMode.LINE) {
                            path.lineTo(end.x, end.y)
                        }

                        if (highlightingAtlas.pathFromRootStates.contains(state)) {
                            val strokePaint = highlightedIncomingPath.apply {
                                mode = PaintMode.STROKE
                                strokeWidth = entry.paint.strokeWidth
                            }
                            canvas.nativeCanvas.drawPath(path, strokePaint)
                        } else if (highlightingAtlas.outgoingPathsState.contains(state)) {
                            val strokePaint = highlightedOutgoingPath.apply {
                                mode = PaintMode.STROKE
                                strokeWidth = entry.paint.strokeWidth
                            }
                            canvas.nativeCanvas.drawPath(path, strokePaint)
                        } else {
                            canvas.nativeCanvas.drawPath(path, entry.paint)
                        }
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
            val cy = coordinate.y
            val entry = drawAtlas.getState(selectedState)
            canvas.nativeCanvas.drawCircle(cx, cy, entry.size + 15f, selectedPaint)
        }
    }
}
