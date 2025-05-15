package moritz.lindner.masterarbeit.drawing

import moritz.lindner.masterarbeit.drawing.layout.Rectangle
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode

data class Coordinate(
    val x: Float,
    val y: Float,
)

data class NodePlacementInformation<T : Comparable<T>>(
    val coordinate: Coordinate,
    val node: EPATreeNode<T>,
) {
    fun isInside(rect: Rectangle): Boolean {
        val (x, y) = this.coordinate

        return x > rect.topLeft.x && y < (rect.topLeft.y * -1) && x < rect.bottomRight.x && y > (rect.bottomRight.y * -1)
    }
}
