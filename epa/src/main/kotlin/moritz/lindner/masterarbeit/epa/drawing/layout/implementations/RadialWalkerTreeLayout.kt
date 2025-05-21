package moritz.lindner.masterarbeit.epa.drawing.layout.implementations

import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.Point
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.drawing.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.placement.Coordinate
import moritz.lindner.masterarbeit.epa.drawing.placement.NodePlacementInformation
import moritz.lindner.masterarbeit.epa.drawing.placement.Rectangle
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class RadialWalkerTreeLayout<T : Comparable<T>>(
    val layerSpace: Float,
    expectedCapacity: Int = 1000,
    val margin: Float,
) : WalkerTreeLayout<T>(
        distance = 10f,
        yDistance = 1.0f,
        expectedCapacity = expectedCapacity,
    ),
    RadialTreeLayout<T> {
    private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

    private lateinit var finalRTree: RTree<NodePlacementInformation<T>, Point>

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

    override fun build(tree: EPATreeNode<T>) {
        super.build(tree)
        logger.info { "assign angles" }
        convertToAngles()

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

    override fun getCircleRadius(): Float = layerSpace

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
