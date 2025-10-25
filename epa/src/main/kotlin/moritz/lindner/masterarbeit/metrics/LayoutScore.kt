package moritz.lindner.masterarbeit.metrics

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class LayoutScore(
    private val gridSize: Int,
) {
    fun scoreLayouts(layouts: List<TreeLayout>): Map<TreeLayout, Result> {
        val scores =
            layouts.map { layout ->
                val area = area(layout)
                val densityScore = density(layout)
                Pair(area, densityScore)
            }

        val normalizedAreaScores =
            minMaxNormalize(
                scores.map { (area, _) -> area.toDouble() },
            ).map { 1.0 - it }

        val normalizedDensityScores =
            minMaxNormalize(
                scores.map { (_, score) -> score },
            )

        val results =
            layouts
                .mapIndexed { index, layout ->
                    val areaScore = normalizedAreaScores[index]
                    val densityScore = normalizedDensityScores[index]

                    layout to
                            Result(
                                area = scores[index].first.toDouble(),
                                areaScore = areaScore,
                                density = scores[index].second,
                                densityScore = densityScore,
                            )
                }.toMap()

        csvWriter().open("./results.csv") {
            writeRow("", "area", "normalized area", "variance", "normalized uniformity")

            results.forEach { (layout, result) ->
                writeRow(
                    layout::class.java.simpleName,
                    result.area,
                    result.areaScore,
                    result.density,
                    result.densityScore
                )
            }
        }

        return results
    }

    fun minMaxNormalize(values: List<Double>): List<Double> {
        val max = values.max()
        val min = values.min()

        return values.map { x ->
            (x - min) / (max - min)
        }
    }

    fun area(layout: TreeLayout): Float {
        val boundingBox = boundingBox(layout)

        val length = abs(boundingBox.topLeft.x - boundingBox.bottomRight.x)
        val height = abs(boundingBox.topLeft.y - boundingBox.bottomRight.y)

        return length * height
    }

    fun boundingBox(layout: TreeLayout): Rectangle {
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        layout.forEach { nodePlacement ->
            minX = min(minX, nodePlacement.coordinate.x)
            maxX = max(maxX, nodePlacement.coordinate.x)

            minY = min(minY, nodePlacement.coordinate.y)
            maxY = max(maxY, nodePlacement.coordinate.y)
        }

        return Rectangle(
            topLeft = Coordinate(minX, maxY),
            bottomRight = Coordinate(maxX, minY),
        )
    }

    fun density(layout: TreeLayout): Double {
        val boundingBox = boundingBox(layout)

        val cellWidth = (boundingBox.bottomRight.x - boundingBox.topLeft.x) / gridSize
        val cellHeight = (boundingBox.bottomRight.y - boundingBox.topLeft.y) / gridSize

        val grid = Array(gridSize) { IntArray(gridSize) }

        layout.forEach { node ->
            val x = node.coordinate.x
            val y = node.coordinate.y

            val gridX = ((x - boundingBox.topLeft.x) / cellWidth).toInt().coerceIn(0, gridSize - 1)
            val gridY = ((y - boundingBox.topLeft.y) / cellHeight).toInt().coerceIn(0, gridSize - 1)

            grid[gridX][gridY] += 1
        }

        val allCounts = grid.flatMap { it.toList() }
        val mean = allCounts.average()
        val variance = allCounts.map { (it - mean).let { d -> d * d } }.average()
        val stdDev = sqrt(variance)

        return 1.0 / stdDev
    }
}
