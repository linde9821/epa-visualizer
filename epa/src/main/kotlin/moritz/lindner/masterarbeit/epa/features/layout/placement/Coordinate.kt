package moritz.lindner.masterarbeit.epa.features.layout.placement

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Represents a 2D coordinate with floating-point precision.
 *
 * @property x The horizontal position.
 * @property y The vertical position.
 */
data class Coordinate(
    val x: Float,
    val y: Float,
) {
    fun distanceTo(other: Coordinate): Float {
        return sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
    }

    fun vectorTo(other: Coordinate): Vector2D {
        return Vector2D(other.x - x, other.y - y)
    }

    fun move(vector: Vector2D): Coordinate {
        return Coordinate(x + vector.x, y + vector.y)
    }
}
