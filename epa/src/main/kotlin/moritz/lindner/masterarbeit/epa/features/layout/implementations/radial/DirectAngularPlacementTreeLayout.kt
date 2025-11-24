package moritz.lindner.masterarbeit.epa.features.layout.implementations.radial

import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RTreeBuilder
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RTreeBuilder.toRTreeRectangle
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import moritz.lindner.masterarbeit.epa.features.layout.tree.EPATreeNode
import kotlin.math.PI
import kotlin.math.max

/**
 * A radial tree layout using direct angular partitioning per node.
 *
 * Unlike Walker-based layouts, this layout recursively assigns angular
 * ranges to each subtree and places nodes evenly along concentric circles
 * based on their depth. Each child of a node receives a proportion of
 * its parent’s angle sector, ensuring a balanced circular appearance.
 *
 * @property layerSpace The radial distance between concentric depth
 *    layers.
 * @property expectedCapacity Expected number of nodes, used to preallocate
 *    internal maps.
 */
class DirectAngularPlacementTreeLayout(
    private val tree: EPATreeNode,
    private val layerSpace: Float,
    private val rotation: Float,
    expectedCapacity: Int = 10000,
) : RadialTreeLayout {
    private val nodePlacementByState = HashMap<State, NodePlacement>(expectedCapacity)
    private lateinit var rTree: RTree<NodePlacement, PointFloat>

    private var isBuilt = false
    private var maxDepth = 0

    override fun build(progressCallback: EpaProgressCallback?) {
        progressCallback?.onProgress(0, 1, "Build Layout")
        assignAngles(tree, 0f, 2f * PI.toFloat())

        rTree = RTreeBuilder.build(nodePlacementByState.values.toList(), progressCallback)
        isBuilt = true
        progressCallback?.onProgress(1, 1, "Build Layout")
    }

    private fun assignAngles(
        treeNode: EPATreeNode,
        start: Float,
        end: Float,
    ) {
        maxDepth = max(maxDepth, treeNode.depth)

        if (treeNode.parent == null) {
            nodePlacementByState[treeNode.state] = NodePlacement(Coordinate(0f, 0f), treeNode.state)
        } else {
            val radius = layerSpace * treeNode.depth
            val theta = ((start + end) / 2f) + rotation

            nodePlacementByState[treeNode.state] = NodePlacement(
                Coordinate.fromPolar(radius, theta),
                treeNode.state,
            )
        }

        val anglePerChild = (end - start) / (treeNode.children().size.toFloat())

        treeNode.children().forEach { child ->
            val childStart = start + child.number() * anglePerChild
            val childEnd = childStart + anglePerChild
            assignAngles(child, childStart, childEnd)
        }
    }

    override fun getCircleRadius(): Float = layerSpace

    override fun getCoordinate(state: State): Coordinate = nodePlacementByState[state]!!.coordinate

    override fun getMaxDepth(): Int = maxDepth

    override fun isBuilt(): Boolean = isBuilt

    override fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacement> {
        return rTree.search(rectangle.toRTreeRectangle()).map { it.value() }
    }

    override fun iterator(): Iterator<NodePlacement> = nodePlacementByState.values.iterator()
}
