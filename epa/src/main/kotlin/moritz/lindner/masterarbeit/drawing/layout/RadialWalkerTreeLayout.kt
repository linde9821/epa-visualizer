package moritz.lindner.masterarbeit.drawing.layout

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.drawing.Coordinate
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class RadialWalkerTreeLayout<T : Comparable<T>>(
    val depthDistance: Float,
    expectedCapacity: Int = 1000,
    val margin: Float,
) : WalkerTreeLayout<T>(
        distance = 10f,
        yDistance = 1.0f,
        expectedCapacity = expectedCapacity,
    ) {
    private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

    private val logger = KotlinLogging.logger {}
    private val usableAngle =
        2 * PI.toFloat() - margin

    private fun convertToAngles() {
        nodePlacementInformationByState.replaceAll { _, nodePlacementInformation ->
            val (cartesianCoordinate, node) = nodePlacementInformation
            val (x, _) = cartesianCoordinate

            val normalizedX = (x - xMin) / (xMax - xMin)
            val radius = node.depth * depthDistance
            val theta = normalizedX * usableAngle

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
}
