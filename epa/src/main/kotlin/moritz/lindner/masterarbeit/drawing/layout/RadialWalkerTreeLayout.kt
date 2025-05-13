package moritz.lindner.masterarbeit.drawing.layout

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.drawing.Coordinate
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class RadialWalkerTreeLayout<T : Comparable<T>>(
    distance: Float,
    private val depthDistance: Float,
    margin: Float = 0.1f * PI.toFloat(),
    private val rotate: Float = 0.5f * PI.toFloat(),
) : WalkerTreeLayout<T>(distance, 1.0f) {
    private val logger = KotlinLogging.logger {}
    private val usableAngle = 2 * PI.toFloat() * (1 - margin)
    private val angleOffset = PI.toFloat() * margin

    private fun convertToAngles() {
        nodePlacementInformationByState.replaceAll { _, nodePlacementInformation ->
            val (coordinate, node) = nodePlacementInformation
            val (x, y) = coordinate

            val normalizedX = (x - xMin) / (xMax - xMin)
            require(!normalizedX.isNaN())
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
