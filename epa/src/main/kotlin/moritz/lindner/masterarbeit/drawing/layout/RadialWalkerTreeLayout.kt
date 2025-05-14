package moritz.lindner.masterarbeit.drawing.layout

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.drawing.Coordinate
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class RadialWalkerTreeLayout<T : Comparable<T>>(
    val depthDistance: Float,
    margin: Float = 0.1f * PI.toFloat(),
    private val rotate: Float = 0.5f * PI.toFloat(),
    expectedCapacity: Int = 1000,
) : WalkerTreeLayout<T>(
        distance = 10f,
        yDistance = 1.0f,
        expectedCapacity = expectedCapacity,
    ) {
    private val logger = KotlinLogging.logger {}
    private val usableAngle = 2 * PI.toFloat() * (1 - margin)
    private val angleOffset = PI.toFloat() * margin

    private fun convertToAngles() {
        nodePlacementInformationByState.replaceAll { _, nodePlacementInformation ->
            val (cartasianCoordinate, node) = nodePlacementInformation
            val (x, y) = cartasianCoordinate

            val normalizedX = (x - xMin) / (xMax - xMin)
            val radius = node.depth * depthDistance
            val theta = rotate + angleOffset + normalizedX * usableAngle

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
