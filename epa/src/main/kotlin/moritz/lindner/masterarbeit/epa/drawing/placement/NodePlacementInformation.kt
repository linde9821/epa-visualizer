package moritz.lindner.masterarbeit.epa.drawing.placement

import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode

data class NodePlacementInformation<T : Comparable<T>>(
    val coordinate: Coordinate,
    val node: EPATreeNode<T>,
)
