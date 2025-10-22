package moritz.lindner.masterarbeit.epa.features.layout.implementations

import com.github.davidmoten.rtree2.Entry
import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor
import smile.manifold.umap
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class PartitionFeatureEmbedding(
    private val progressCallback: EpaProgressCallback? = null
) : AutomatonVisitor<Long> {
    override fun onProgress(current: Long, total: Long) {
        progressCallback?.onProgress(current, total, "Compute normalized frequency of events per partition")
    }

    fun getFeatureEmbedding(): Map<Int, DoubleArray> {
        TODO()
    }
}


class SemanticRadialLayout(
    private val epa: ExtendedPrefixAutomaton<Long>,
    margin: Float,
    private val layerSpace: Float = 1f,
    private val rotation: Float = 0f,
) : RadialTreeLayout {

    private val epaService = EpaService<Long>()
    private var isBuiltFlag = false
    private lateinit var rTree: RTree<NodePlacement, PointFloat>

    private val nodePlacementByState = HashMap<State, NodePlacement>(epa.states.size)
    private var maxDepth = Int.MIN_VALUE

    private val usableAngle =
        2 * PI.toFloat() - margin

    override fun getCircleRadius(): Float {
        return layerSpace
    }

    override fun getMaxDepth(): Int {
        return maxDepth
    }

    override fun build(progressCallback: EpaProgressCallback?) {
        val embeddings = buildEmbeddings()
        convertToAngles(reduceDimensions(embeddings))
        rTree = RTreeBuilder.build(nodePlacementByState.values.toList(), progressCallback)
        isBuiltFlag = true
    }

    private fun buildEmbeddings(): Map<Int, DoubleArray> {
        return epa.states.map { epa.partition(it) }.distinct().associateWith { partition ->
            doubleArrayOf(partition.toDouble())
        }
    }

    private fun convertToAngles(angleByPartition: Map<Int, Float>) {
        epa.states.forEach { state ->
            val y = epaService.getDepth(state)
            maxDepth = max(maxDepth, y)

            val radius = y * layerSpace
            val theta = angleByPartition[epa.partition(state)] ?: 0f

            nodePlacementByState[state] = NodePlacement(
                coordinate = Coordinate(
                    x = radius * cos(theta),
                    y = radius * sin(theta),
                ),
                state = state
            )
        }
    }

    fun calculateAngle(x: Double, y: Double): Float {
        var angle = atan2(y, x)

        // Convert from [-π, π] to [0, 2π]
        if (angle < 0) {
            angle += 2 * Math.PI
        }

        return angle.toFloat()
    }

    private fun reduceDimensions(
        embeddings: Map<Int, DoubleArray>
    ): Map<Int, Float> {
        val partitions = embeddings.keys.toList()
        val matrix = partitions.map { embeddings[it]!! }.toTypedArray()

        // Check minimum requirements
        if (partitions.size < 3) {
            // Too few states for dimensionality reduction
            // return handleSmallDataset(states, matrix)
        }

        // TODO: add others
        val coordinates2D = computeUmap(matrix)
        return partitions.zip(coordinates2D).associate { (partition, coordinate) ->
            partition to calculateAngle(coordinate[0], coordinate[1])
        }
    }

    private fun computeUmap(matrix: Array<DoubleArray>): Array<DoubleArray> {
        return umap(
            data = matrix,
            d = 2,
            k = 5,
            epochs = 300,
        )
    }

    override fun isBuilt(): Boolean {
        return isBuiltFlag
    }

    override fun getCoordinate(state: State): Coordinate {
        return nodePlacementByState[state]?.coordinate ?: throw IllegalStateException(
            "No coodinate for $state present",
        )
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
        return search.map(Entry<NodePlacement, PointFloat>::value)
    }

    override fun iterator(): Iterator<NodePlacement> {
        return nodePlacementByState.values.iterator()
    }
}