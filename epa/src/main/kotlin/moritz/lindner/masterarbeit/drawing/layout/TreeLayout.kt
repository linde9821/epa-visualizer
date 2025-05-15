package moritz.lindner.masterarbeit.drawing.layout

import moritz.lindner.masterarbeit.drawing.Coordinate
import moritz.lindner.masterarbeit.drawing.NodePlacementInformation
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode
import moritz.lindner.masterarbeit.epa.domain.State

data class Rectangle(
    val topLeft: Coordinate,
    val bottomRight: Coordinate,
)

interface TreeLayout<T : Comparable<T>> {
    fun build(tree: EPATreeNode<T>)

    fun getCoordinate(state: State): Coordinate

    fun search(boundingBox: Rectangle): List<NodePlacementInformation<T>>

    fun getMaxDepth(): Int

    fun isBuilt(): Boolean
}
