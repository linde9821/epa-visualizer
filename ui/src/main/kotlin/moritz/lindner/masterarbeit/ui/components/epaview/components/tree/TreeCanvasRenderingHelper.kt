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
import moritz.lindner.masterarbeit.epa.features.lod.LODQuery
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DrawAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.highlight.HighlightingAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.labels.StateLabels
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import org.jetbrains.skia.Paint
import kotlin.math.cos
import kotlin.math.pow
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

            if (highlightingAtlas.highlightedStates.contains(state)) {
                canvas.nativeCanvas.drawCircle(cx, cy, entry.size + 15f, drawAtlas.highlightedPaint)
            }

            canvas.nativeCanvas.drawCircle(cx, cy, entry.size, paint)

            if (entry.size * scale >= drawAtlas.stateSizeUntilLabelIsDrawn) {
                val label = stateLabels.getLabelForState(state)
                canvas.nativeCanvas.drawImage(
                    label,
                    cx + entry.size + 5f,
                    cy - label.height / 2f,
                )
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

                val fromCoord = layout.getCoordinate(timedState.state)
                val toCoord = timedState.nextState?.let { layout.getCoordinate(it) }

                val tokenPosition = if (toCoord != null) {
                    val (c1, c2) = getControlPoints(fromCoord, toCoord, 0.5f)
                    interpolateBezier(
                        start = Offset(fromCoord.x, fromCoord.y),
                        c1 = Offset(c1.x, c1.y),
                        c2 = Offset(c2.x, c2.y),
                        end = Offset(toCoord.x, toCoord.y),
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
            y = this.y
        )
    }
}