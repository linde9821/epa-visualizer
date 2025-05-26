package moritz.lindner.masterarbeit.epa.drawing.layout.implementations

import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.Point
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import com.github.davidmoten.rtree2.internal.EntryDefault
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

class DirectAngularPlacementTreeLayout(
    private val layerSpace: Float,
    expectedCapacity: Int = 1000,
) : RadialTreeLayout {
    protected val nodePlacementInformationByState = HashMap<State, NodePlacementInformation>(expectedCapacity)

    private lateinit var finalRTree: RTree<NodePlacementInformation, Point>

    private var isBuilt = false
    private var maxDepth = 0

    override fun build(tree: EPATreeNode) {
        assignAngles(tree, 0f, 2f * PI.toFloat())

        finalRTree =
            RTree.create(
                nodePlacementInformationByState.map { (_, info) ->
                    EntryDefault(
                        info,
                        PointFloat.create(
                            info.coordinate.x,
                            info.coordinate.y * -1,
                        ),
                    )
                },
            )

        isBuilt = true
    }

    private fun assignAngles(
        tree: EPATreeNode,
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
            assignAngles(child, childStart, childEnd)
        }
    }

    override fun getCircleRadius(): Float = layerSpace

    override fun getCoordinate(state: State): Coordinate = nodePlacementInformationByState[state]!!.coordinate

    override fun getMaxDepth(): Int = maxDepth

    override fun isBuilt(): Boolean = isBuilt

    override fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacementInformation> {
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
