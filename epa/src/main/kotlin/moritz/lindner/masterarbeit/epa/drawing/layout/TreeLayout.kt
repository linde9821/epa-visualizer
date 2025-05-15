package moritz.lindner.masterarbeit.epa.drawing.layout

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.drawing.placement.Coordinate
import moritz.lindner.masterarbeit.epa.drawing.placement.NodePlacementInformation
import moritz.lindner.masterarbeit.epa.drawing.placement.Rectangle
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode

interface TreeLayout<T : Comparable<T>> {
    fun build(tree: EPATreeNode<T>)

    fun getCoordinate(state: State): Coordinate

    fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacementInformation<T>>

    fun getMaxDepth(): Int

    fun isBuilt(): Boolean
}
