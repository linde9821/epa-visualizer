package moritz.lindner.masterarbeit.drawing

import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode

data class Coordinate(
    val x: Float,
    val y: Float,
)

data class NodePlacementInformation<T : Comparable<T>>(
    val coordinate: Coordinate,
    val node: EPATreeNode<T>,
)
