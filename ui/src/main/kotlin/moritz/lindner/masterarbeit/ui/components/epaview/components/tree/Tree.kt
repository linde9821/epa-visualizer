package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import org.jetbrains.skia.Surface

class Tree(
) {
    private val stateLabels = StateLabels(
        backgroundColor = Color.WHITE,
        baseFontSize = 21f,
    )

    val redFill =
        Paint().apply {
            color = Color.RED
            mode = PaintMode.FILL
            isAntiAlias = true
        }

    val blackFill =
        Paint().apply {
            color = Color.BLACK
            mode = PaintMode.FILL
            isAntiAlias = true
        }

    val redStroke =
        Paint().apply {
            color = Color.RED
            mode = PaintMode.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }

    val blackStroke =
        Paint().apply {
            color = Color.BLACK
            mode = PaintMode.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }

    fun calculateDimensions(layout: TreeLayout, stateLabels: StateLabels): Pair<Int, Int> {
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        layout.getAllCoordinates().forEach { (coordinate, node) ->
            val state = node.state
            if (state is PrefixState) {
                val x = coordinate.x
                val y = -coordinate.y // Note: you're flipping Y

                // Account for circle radius
                val circleRadius = 10f
                minX = minOf(minX, x - circleRadius)
                maxX = maxOf(maxX, x + circleRadius)
                minY = minOf(minY, y - circleRadius)
                maxY = maxOf(maxY, y + circleRadius)

                // Account for label dimensions
                stateLabels.getLabelForState(state)?.let { labelImage ->
                    val labelRight = x + circleRadius + 5f + labelImage.width
                    val labelTop = y - labelImage.height / 2f
                    val labelBottom = y + labelImage.height / 2f

                    maxX = maxOf(maxX, labelRight)
                    minY = minOf(minY, labelTop)
                    maxY = maxOf(maxY, labelBottom)
                }
            }
        }

        // Add some padding
        val padding = 20f
        minX -= padding
        minY -= padding
        maxX += padding
        maxY += padding
        val calculatedWidth = (maxX - minX).toInt()
        val calculatedHeight = (maxY - minY).toInt()
        return Pair(calculatedWidth, calculatedHeight)
    }

    suspend fun generateTree(epa: ExtendedPrefixAutomaton<Long>, layout: TreeLayout): Image {
        epa.states.forEachIndexed { index, state ->
            stateLabels.generateLabelForState(state)
            if (index % 100 == 0) yield()
        }

        val coordinates = layout.getAllCoordinates()
        if (coordinates.isEmpty()) {
            val surface = Surface.makeRasterN32Premul(100, 100)
            return surface.makeImageSnapshot()
        }

        // Calculate bounding box in the FLIPPED coordinate system
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        coordinates.forEach { (coordinate, node) ->
            val x = coordinate.x
            val y = -coordinate.y  // Flip Y first

            minX = minOf(minX, x)
            maxX = maxOf(maxX, x)
            minY = minOf(minY, y)
            maxY = maxOf(maxY, y)
        }

        // Add padding
        val padding = 50f
        minX -= padding
        minY -= padding
        maxX += padding
        maxY += padding

        val calculatedWidth = (maxX - minX).toInt()
        val calculatedHeight = (maxY - minY).toInt()

        println("Image dimensions: ${calculatedWidth}x${calculatedHeight}")
        println("Bounds: minX=$minX, maxX=$maxX, minY=$minY, maxY=$maxY")

        val surface = Surface.makeRasterN32Premul(calculatedWidth, calculatedHeight)
        val canvas = surface.canvas
        canvas.clear(Color.WHITE)

        coordinates.forEach { (coordinate, node) ->
            val state = node.state

            if (state is PrefixState) {
                // Transform coordinates to image space
                val cx = coordinate.x - minX
                val cy = -coordinate.y - minY  // Flip Y, then translate

                val parentCoordinate = layout.getCoordinate(state.from)
                val start = Offset(parentCoordinate.x - minX, -parentCoordinate.y - minY)
                val end = Offset(cx, cy)

                // Fix control points transformation too
                val (c1, c2) = getControlPoints(parentCoordinate, coordinate, 0.5f)
                val transformedC1 = Offset(c1.x - minX, -c1.y - minY)
                val transformedC2 = Offset(c2.x - minX, -c2.y - minY)

                val path = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(transformedC1.x, transformedC1.y, transformedC2.x, transformedC2.y, end.x, end.y)
                }

                canvas.drawPath(path, blackStroke)

                val circleRadius = 10f
                canvas.drawCircle(cx, cy, circleRadius, blackFill)

//                stateLabels.getLabelForState(state)?.let { labelImage ->
//                    canvas.drawImage(
//                        labelImage,
//                        cx + circleRadius + 5f,
//                        cy - labelImage.height / 2f,
//                    )
//                }
            }
        }

        return surface.makeImageSnapshot()
    }
}