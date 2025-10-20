package moritz.lindner.masterarbeit.epa.features.layout.placement

/**
 * Represents a rectangle defined by its top-left and bottom-right
 * coordinates.
 *
 * Assumes a standard Cartesian coordinate system where `topLeft.y <
 * bottomRight.y` and `topLeft.x < bottomRight.x`.
 *
 * @property topLeft The upper-left corner of the rectangle.
 * @property bottomRight The lower-right corner of the rectangle.
 */
data class Rectangle(
    val topLeft: Coordinate,
    val bottomRight: Coordinate,
) {
    fun contains(coord: Coordinate): Boolean {
        val minX = minOf(topLeft.x, bottomRight.x)
        val maxX = maxOf(topLeft.x, bottomRight.x)
        val minY = minOf(topLeft.y, bottomRight.y)
        val maxY = maxOf(topLeft.y, bottomRight.y)

        return coord.x in minX..maxX && coord.y in minY..maxY
    }
}
