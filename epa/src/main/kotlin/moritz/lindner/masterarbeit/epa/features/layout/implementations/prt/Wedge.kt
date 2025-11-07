package moritz.lindner.masterarbeit.epa.features.layout.implementations.prt

import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import kotlin.math.cos
import kotlin.math.sin

data class Wedge(
    val center: Coordinate,
    val radius: Float,
    val angleRange: Pair<Float, Float>,
) {
    fun arcMidpoint(): Coordinate {
        val (startAngle, endAngle) = angleRange
        val midAngle = (startAngle + endAngle) / 2.0

        return Coordinate(
            x = (center.x + radius * cos(midAngle)).toFloat(),
            y = (center.y + radius * sin(midAngle)).toFloat()
        )
    }
}