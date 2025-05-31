package moritz.lindner.masterarbeit.epa.drawing.placement

/**
 * Represents a rectangle defined by its top-left and bottom-right coordinates.
 *
 * Assumes a standard Cartesian coordinate system where `topLeft.y < bottomRight.y`
 * and `topLeft.x < bottomRight.x`.
 *
 * @property topLeft The upper-left corner of the rectangle.
 * @property bottomRight The lower-right corner of the rectangle.
 */
data class Rectangle(
    val topLeft: Coordinate,
    val bottomRight: Coordinate,
)
