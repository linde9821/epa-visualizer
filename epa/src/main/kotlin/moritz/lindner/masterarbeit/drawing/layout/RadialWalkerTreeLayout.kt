package moritz.lindner.masterarbeit.drawing.layout

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.drawing.Coordinate
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

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

    private val logger = KotlinLogging.logger {}
    private val usableAngle =
        2 * PI.toFloat() - margin

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
    }

    override fun getCircleRadius(): Float = layerSpace

    override fun isBuilt(): Boolean = super.isBuilt()
}
