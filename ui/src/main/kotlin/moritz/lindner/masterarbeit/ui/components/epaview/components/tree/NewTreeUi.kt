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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.features.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.NewTreeUi.drawDepthCircles
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.NewTreeUi.getControlPoints
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.NewTreeUi.toCoordinate
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.DrawAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.state.TabState
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.skia.Path
import kotlin.math.pow

@Composable
fun NewTreeUi(
    treeLayout: TreeLayout,
    stateLabels: StateLabels,
    drawAtlas: DrawAtlas,
    onStateHover: (State?) -> Unit,
    onStateClicked: (State?) -> Unit,
    tabState: TabState,
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var hoveredNode by remember(treeLayout) { mutableStateOf<NodePlacement?>(null) }
    var pressedNode by remember(treeLayout) { mutableStateOf<NodePlacement?>(null) }

    LaunchedEffect(tabState.locateState) {
        if (tabState.locateState != null) {

            val targetNode = treeLayout.getCoordinate(tabState.locateState)

            val screenCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            offset = screenCenter - targetNode.toOffset() * scale
        }
    }

    LaunchedEffect(hoveredNode) {
        onStateHover(hoveredNode?.node?.state)
    }

    LaunchedEffect(pressedNode) {
        onStateClicked(pressedNode?.node?.state)
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
                    val scrollDelta =
                        event.changes
                            .firstOrNull()
                            ?.scrollDelta
                            ?.y ?: 0f

                    if (event.type == PointerEventType.Scroll && scrollDelta != 0f) {
                        val cursorPosition = event.changes.first().position
                        val worldPosBefore = (cursorPosition - offset) / scale

                        val oldScale = scale
                        val newScale = (oldScale * if (scrollDelta < 0) 1.1f else 0.9f).coerceIn(0.01f, 14f)
                        scale = newScale

                        val worldPosAfter = worldPosBefore * scale
                        offset = cursorPosition - worldPosAfter
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

                        // Transform screen coordinates to world coordinates
                        val worldPosition = (screenPosition - offset) / scale

                        val width = 10
                        val nodeAtPosition =
                            treeLayout.getCoordinatesInRectangle(
                                Rectangle(
                                    topLeft = Coordinate(worldPosition.x - width, worldPosition.y - width),
                                    bottomRight = Coordinate(worldPosition.x + width, worldPosition.y + width),
                                ),
                            )

                        // Update hovered node if it changed
                        val newNode = nodeAtPosition.firstOrNull()

                        if (newNode != hoveredNode) {
                            hoveredNode = newNode
                        }
                        if (event.type == PointerEventType.Press && pressedNode != newNode) {
                            pressedNode = hoveredNode
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
                drawEPANew(
                    layout = treeLayout,
                    boundingBox = compouteBoundingBox(offset, scale),
                    stateLabels = stateLabels,
                    scale = scale,
                    drawAtlas = drawAtlas,
                    tabState = tabState
                )
            } catch (e: Exception) {
                logger.error(e) { "error while drawing: $e" }
            }
        }
    }
}

private fun DrawScope.compouteBoundingBox(
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

private fun Coordinate.toOffset(): Offset {
    return Offset(
        x = this.x,
        y = this.y * -1
    )
}

fun DrawScope.drawEPANew(
    layout: TreeLayout,
    boundingBox: Rectangle,
    stateLabels: StateLabels,
    scale: Float,
    drawAtlas: DrawAtlas,
    tabState: TabState
) {
    val visibleNodes = layout.getCoordinatesInRectangle(boundingBox)

    drawIntoCanvas { canvas ->

        (layout as? RadialTreeLayout)?.let {
            drawDepthCircles(layout)
        }

        visibleNodes.forEach { (coordinate, node) ->
            val state = node.state

            if (state is PrefixState) {
                val cx = coordinate.x
                val cy = -coordinate.y
                val parentCoordinate = layout.getCoordinate(state.from)
                val paint = drawAtlas.getTransitionEntryByParentState(state.from)

                val start = Offset(parentCoordinate.x, -parentCoordinate.y)
                val end = Offset(cx, cy)
                val (c1, c2) = getControlPoints(parentCoordinate, coordinate, 0.5f)

                val path = Path()
                    .apply {
                        moveTo(start.x, start.y)
                        cubicTo(c1.x, -c1.y, c2.x, -c2.y, end.x, end.y)
                    }

                canvas.nativeCanvas.drawPath(path, paint.paint)
            }
        }

        // draw nodes
        visibleNodes.forEach { (coordinate, node) ->
            val state = node.state
            val entry = drawAtlas.getState(state)

            val cx = coordinate.x
            val cy = -coordinate.y

            canvas.nativeCanvas.drawCircle(cx, cy, entry.size, entry.paint)

            if (node.state == tabState.selectedState) {
                canvas.nativeCanvas.drawCircle(cx, cy, entry.size + 15f, drawAtlas.selectedStatePaint)
            }

            val screenRadius = entry.size * scale
            if (screenRadius >= 10f) {
                stateLabels.getLabelForState(state)?.let { labelImage ->
                    canvas.nativeCanvas.drawImage(
                        labelImage,
                        cx + entry.size + 5f,
                        cy - labelImage.height / 2f, // vertically center
                    )
                }
            }
        }
    }
}

object NewTreeUi {

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
}