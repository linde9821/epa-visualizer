package moritz.lindner.masterarbeit.drawing.layout.implementations

import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.Point
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import moritz.lindner.masterarbeit.drawing.Coordinate
import moritz.lindner.masterarbeit.drawing.NodePlacementInformation
import moritz.lindner.masterarbeit.drawing.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.drawing.layout.Rectangle
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode
import moritz.lindner.masterarbeit.epa.domain.State
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class SimpleTreeLayout<T : Comparable<T>>(
    private val layerSpace: Float,
) : RadialTreeLayout<T> {
    protected val nodePlacementInformationByState = HashMap<State, NodePlacementInformation<T>>()

    private lateinit var finalRTree: RTree<NodePlacementInformation<T>, Point>

    private var isBuilt = false
    private var maxDepth = 0

    override fun build(tree: EPATreeNode<T>) {
        walk(tree, 0f, 2f * PI.toFloat())

        var rTree = RTree.create<NodePlacementInformation<T>, Point>()

        nodePlacementInformationByState.forEach { (state, info) ->
            rTree =
                rTree.add(
                    info,
                    PointFloat.create(
                        info.coordinate.x,
                        info.coordinate.y * -1,
                    ),
                )
        }

        finalRTree = rTree
        isBuilt = true
    }

    private fun walk(
        tree: EPATreeNode<T>,
        start: Float,
        end: Float,
    ) {
        maxDepth = max(maxDepth, tree.depth)

        if (tree.parent == null) {
            nodePlacementInformationByState[tree.state] = NodePlacementInformation(Coordinate(0f, 0f), tree)
        } else {
            val radius = layerSpace * tree.depth
            val theta = (start + end) / 2f

            nodePlacementInformationByState[tree.state] =
                NodePlacementInformation(
                    Coordinate(
                        x = radius * cos(theta),
                        y = radius * sin(theta),
                    ),
                    tree,
                )
        }

        val anglePerChild = (end - start) / (tree.children().size.toFloat())

        tree.children().forEach { child ->
            val childStart = start + child.number() * anglePerChild
            val childEnd = childStart + anglePerChild
            walk(child, childStart, childEnd)
        }
    }

    override fun getCircleRadius(): Float = layerSpace

    override fun getCoordinate(state: State): Coordinate = nodePlacementInformationByState[state]!!.coordinate

    override fun getMaxDepth(): Int = maxDepth

    override fun isBuilt(): Boolean = isBuilt

    override fun search(boundingBox: Rectangle): List<NodePlacementInformation<T>> {
        val search =
            finalRTree
                .search(
                    Geometries.rectangle(
                        boundingBox.topLeft.x,
                        boundingBox.topLeft.y,
                        boundingBox.bottomRight.x,
                        boundingBox.bottomRight.y,
                    ),
                ).toList()
        return search.map { it.value() }
    }
}
