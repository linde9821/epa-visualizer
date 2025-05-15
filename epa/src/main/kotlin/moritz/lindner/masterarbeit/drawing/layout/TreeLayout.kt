package moritz.lindner.masterarbeit.drawing.layout

import moritz.lindner.masterarbeit.drawing.Coordinate
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode
import moritz.lindner.masterarbeit.epa.domain.State

interface TreeLayout<T : Comparable<T>> {
    fun build(tree: EPATreeNode<T>)

    fun getCoordinate(state: State): Coordinate

    fun getMaxDepth(): Int
}

interface RadialTreeLayout<T : Comparable<T>> : TreeLayout<T> {
    fun getCircleRadius(): Float
}
