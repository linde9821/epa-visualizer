package moritz.lindner.masterarbeit.drawing.placement

import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode

data class NodePlacementInformation<T : Comparable<T>>(
    val coordinate: Coordinate,
    val node: EPATreeNode<T>,
)
