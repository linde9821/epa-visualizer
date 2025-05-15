package moritz.lindner.masterarbeit.epa.drawing.layout.implementations

import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.Point
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.drawing.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.placement.Coordinate
import moritz.lindner.masterarbeit.epa.drawing.placement.NodePlacementInformation
import moritz.lindner.masterarbeit.epa.drawing.placement.Rectangle
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class DirectAngularPlacement<T : Comparable<T>>(
    private val layerSpace: Float,
    private val expectedCapacity: Int = 1000,
) : RadialTreeLayout<T> {
    protected val nodePlacementInformationByState = HashMap<State, NodePlacementInformation<T>>(expectedCapacity)

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

    override fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacementInformation<T>> {
        val search =
            finalRTree
                .search(
                    Geometries.rectangle(
                        rectangle.topLeft.x,
                        rectangle.topLeft.y,
                        rectangle.bottomRight.x,
                        rectangle.bottomRight.y,
                    ),
                ).toList()
        return search.map { it.value() }
    }
}
