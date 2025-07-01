package moritz.lindner.masterarbeit.epa.drawing.layout.implementations

import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import com.github.davidmoten.rtree2.internal.EntryDefault
import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.drawing.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.placement.Coordinate
import moritz.lindner.masterarbeit.epa.drawing.placement.NodePlacementInformation
import moritz.lindner.masterarbeit.epa.drawing.placement.Rectangle
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A radial variant of the Walker tree layout algorithm.
 *
 * This layout arranges nodes in concentric circles around the root node, where each tree depth
 * level forms a ring (layer), and sibling nodes are spaced proportionally along the angle of the ring.
 * It first computes a traditional Walker layout in Cartesian coordinates and then transforms it into polar coordinates.
 *
 * @property layerSpace The distance between concentric layers (depth levels).
 * @property expectedCapacity The expected number of nodes, used for internal data structure optimization.
 * @property margin The angular margin (in radians) subtracted from the full circle to avoid overlap or crowding.
 */
class RadialWalkerTreeLayout(
    val layerSpace: Float,
    expectedCapacity: Int = 1000,
    val margin: Float,
) : WalkerTreeLayout(
        distance = 10f,
        yDistance = 1.0f,
        expectedCapacity = expectedCapacity,
    ),
    RadialTreeLayout {
    private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

    private lateinit var finalRTree: RTree<NodePlacementInformation, PointFloat>

    private val logger = KotlinLogging.logger {}
    private val usableAngle =
        2 * PI.toFloat() - margin

    private var isBuilt: Boolean = false

    private fun convertToAngles() {
        nodePlacementInformationByState.replaceAll { _, nodePlacementInformation ->
            val (cartesianCoordinate, node) = nodePlacementInformation
            val (x, _) = cartesianCoordinate

            val normalizedX = (x - xMin) / (xMax - xMin)
            val radius = node.depth * layerSpace
            val theta = (normalizedX * usableAngle) + 90f.degreesToRadians()

            nodePlacementInformation.copy(
                coordinate =
                    Coordinate(
                        x = radius * cos(theta),
                        y = radius * sin(theta),
                    ),
            )
        }
    }

    override fun build(tree: EPATreeNode) {
        super.build(tree)
        logger.info { "assign angles" }
        convertToAngles()

        val entries =
            nodePlacementInformationByState.map { (_, info) ->
                EntryDefault(
                    info,
                    PointFloat.create(
                        info.coordinate.x,
                        info.coordinate.y * -1,
                    ),
                )
            }
        finalRTree =
            if (entries.size < 10_000) {
                RTree.create(entries)
            } else {
                RTree.star().create(entries)
            }

        isBuilt = true
    }

    override fun getCircleRadius(): Float = layerSpace

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
