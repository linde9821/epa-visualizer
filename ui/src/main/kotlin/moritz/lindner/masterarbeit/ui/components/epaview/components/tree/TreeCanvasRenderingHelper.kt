package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DrawAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.highlight.HighlightingAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.labels.StateLabels
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

object TreeCanvasRenderingHelper {

    fun drawNodes(
        visibleNodes: List<NodePlacement>,
        drawAtlas: DrawAtlas,
        highlightingAtlas: HighlightingAtlas,
        canvas: Canvas,
        scale: Float,
        stateLabels: StateLabels
    ) {
        visibleNodes.forEach { (coordinate, state) ->
            val entry = drawAtlas.getState(state)
            val paint = entry.paint
            val cx = coordinate.x
            val cy = coordinate.y

            if (highlightingAtlas.pathFromRootStates.contains(state)) {
                canvas.nativeCanvas.drawCircle(cx, cy, entry.size + 15f, drawAtlas.pathFromRootPaint)
            } else if (highlightingAtlas.outgoingPathsState.contains(state)) {
                canvas.nativeCanvas.drawCircle(cx, cy, entry.size + 15f, drawAtlas.outgoingPathsPaint)
            }

            canvas.nativeCanvas.drawCircle(cx, cy, entry.size, paint)

            if (entry.size * scale >= drawAtlas.stateSizeUntilLabelIsDrawn) {
                val label = stateLabels.getLabelForState(state)
                val x = cx + entry.size + 5f
                val y = cy - label.height / 2f
                val scale = stateLabels.scale

                val dstRect = Rect.makeXYWH(x, y, label.width.toFloat() / scale, label.height.toFloat() / scale)
                canvas.nativeCanvas.drawImageRect(label, dstRect, paint)

                if (highlightingAtlas.sameActivityStates.contains(state)) {
                    val padding = 6f
                    val rrect = RRect.makeXYWH(
                        x - padding,
                        y - padding,
                        (label.width / scale + (padding * 2)),
                        (label.height / scale + (padding * 2)),
                        8f
                    )

                    canvas.nativeCanvas.drawRRect(rrect, drawAtlas.sameActivityPaint)
                }
            }
        }
    }

    fun drawTokensWithSpreading(
        animationState: AnimationState,
        visibleStates: Set<State>,
        layout: Layout,
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

                val parentCoordinate = layout.getCoordinate(timedState.state)
                val childCoordinate = timedState.nextState?.let { layout.getCoordinate(it) }

                val tokenPosition = if (childCoordinate != null) {


                    val controlPoint = getControlPoint(parentCoordinate, childCoordinate, .33f)

                    interpolate(
                        start = Offset(parentCoordinate.x, parentCoordinate.y),
                        controlPoint = controlPoint,
                        end = Offset(childCoordinate.x, childCoordinate.y),
                        t = progress,
                    )
                } else {
                    Offset(parentCoordinate.x, -parentCoordinate.y)
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

    fun findNodeAt(layout: Layout, worldPos: Offset): NodePlacement? {
        val searchWidth = 10f

        val topLeft = Offset(
            x = worldPos.x - searchWidth,
            y = worldPos.y - searchWidth
        )
        val bottomRight = Offset(
            x = worldPos.x + searchWidth,
            y = worldPos.y + searchWidth
        )

        return layout.getCoordinatesInRectangle(
            Rectangle(topLeft.toCoordinate(), bottomRight.toCoordinate())
        ).firstOrNull()
    }

    fun interpolate(
        start: Offset,
        controlPoint: Offset,
        end: Offset,
        t: Float,
    ): Offset {
        val u = 1 - t

        val x =
            u * u * start.x +
                    2 * u * t * controlPoint.x +
                    t * t * end.x

        val y =
            u * u * start.y +
                    2 * u * t * controlPoint.y +
                    t * t * end.y

        return Offset(x, y)
    }

    fun getControlPoint(
        parent: Coordinate,
        child: Coordinate,
        curvature: Float = 0.5f,
    ): Offset {
        val center = Offset.Zero

        val dx = child.x - parent.x
        val dy = child.y - parent.y
        val length = hypot(dx, dy)

        val mx = (parent.x + child.x) / 2f
        val my = (parent.y + child.y) / 2f

        var nx = -dy / length
        var ny = dx / length

        val toCenter = Offset(center.x - mx, center.y - my)
        val dot = nx * toCenter.x + ny * toCenter.y

        if (dot > 0) {
            nx = -nx
            ny = -ny
        }

        val offset = length * curvature

        return Offset(mx + nx * offset, my + ny * offset)
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

    fun Offset.toCoordinate(): Coordinate {
        return Coordinate(
            x = this.x,
            y = this.y,
        )
    }

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
            y = this.y
        )
    }
}