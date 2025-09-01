package moritz.lindner.masterarbeit.epa.features.layout.implementations

import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import moritz.lindner.masterarbeit.epa.features.layout.tree.EPATreeNode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * A radial tree layout using direct angular partitioning per node.
 *
 * Unlike Walker-based layouts, this layout recursively assigns angular ranges to each subtree
 * and places nodes evenly along concentric circles based on their depth. Each child of a node
 * receives a proportion of its parent’s angle sector, ensuring a balanced circular appearance.
 *
 * @property layerSpace The radial distance between concentric depth layers.
 * @property expectedCapacity Expected number of nodes, used to preallocate internal maps.
 */
class DirectAngularPlacementTreeLayout(
    private val layerSpace: Float,
    private val rotation: Float,
    expectedCapacity: Int = 10000,
) : RadialTreeLayout {
    private val nodePlacementByState = HashMap<State, NodePlacement>(expectedCapacity)
    private lateinit var rTree: RTree<NodePlacement, PointFloat>

    private var isBuilt = false
    private var maxDepth = 0

    override fun build(tree: EPATreeNode) {
        assignAngles(tree, 0f, 2f * PI.toFloat())

        rTree = RTreeBuilder.build(nodePlacementByState.values.toList())
        isBuilt = true
    }

    private fun assignAngles(
        tree: EPATreeNode,
        start: Float,
        end: Float,
    ) {
        maxDepth = max(maxDepth, tree.depth)

        if (tree.parent == null) {
            nodePlacementByState[tree.state] = NodePlacement(Coordinate(0f, 0f), tree)
        } else {
            val radius = layerSpace * tree.depth
            val theta = ((start + end) / 2f) + rotation

            nodePlacementByState[tree.state] =
                NodePlacement(
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
            assignAngles(child, childStart, childEnd)
        }
    }

    override fun getCircleRadius(): Float = layerSpace

    override fun getCoordinate(state: State): Coordinate = nodePlacementByState[state]!!.coordinate

    override fun getMaxDepth(): Int = maxDepth

    override fun isBuilt(): Boolean = isBuilt

    override fun getAllCoordinates(): List<NodePlacement> {
        return nodePlacementByState.values.toList()
    }

    override fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacement> {
        val search =
            rTree
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

    override fun iterator(): Iterator<NodePlacement> = nodePlacementByState.values.iterator()
}
