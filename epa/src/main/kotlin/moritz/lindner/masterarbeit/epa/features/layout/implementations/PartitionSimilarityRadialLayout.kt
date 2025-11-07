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
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.PartitionFeatureEmbedder
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import smile.manifold.umap
import smile.math.MathEx
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class PartitionSimilarityRadialLayout(
    private val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val config: LayoutConfig.PartitionSimilarityRadialLayoutConfig = LayoutConfig.PartitionSimilarityRadialLayoutConfig()
) : RadialTreeLayout {

    private val epaService = EpaService<Long>()
    private var isBuiltFlag = false
    private val nodePlacementByState = HashMap<State, NodePlacement>(extendedPrefixAutomaton.states.size)
    private lateinit var rTree: RTree<NodePlacement, PointFloat>

    private var maxDepth = Int.MIN_VALUE

    override fun build(progressCallback: EpaProgressCallback?) {
        MathEx.setSeed(42);

        val embedder = PartitionFeatureEmbedder()
        progressCallback?.onProgress(0, 2, "Create Embedding")
        val featureEmbeddings = embedder.computeEmbedding(extendedPrefixAutomaton)

        progressCallback?.onProgress(1, 2, "Reduce Dimensions")
        val partitionCoordinates = reduceDimensions(featureEmbeddings)
        val angleByPartition = partitionCoordinates.keys.toList().zip(
//            alignAndConvert(partitionCoordinates.values.toList())
            partitionCoordinates.values.toList().map { toAngle(it) }
        ).toMap()

        extendedPrefixAutomaton.states.forEach { state ->
            val radius = epaService.getDepth(state)

            maxDepth = max(maxDepth, radius)

            val partition = extendedPrefixAutomaton.partition(state)
            val theta = angleByPartition[partition]!!

            nodePlacementByState[state] = NodePlacement(
                coordinate = Coordinate(
                    x = (radius * config.layerSpace) * cos(theta),
                    y = (radius * config.layerSpace) * sin(theta),
                ),
                state = state
            )
        }

        rTree = RTreeBuilder.build(nodePlacementByState.values.toList(), progressCallback)
        isBuiltFlag = true
    }

    fun toAngle(coordinate: Coordinate, minRadius: Float = 0.1f): Float {
        val r = coordinate.distanceTo(Coordinate(0f, 0f))

        // Push points away from origin to stabilize angles
        val adjustedR = max(r, minRadius)
        val adjustedX = if (r > 0) coordinate.x * (adjustedR / r) else minRadius
        val adjustedY = if (r > 0) coordinate.y * (adjustedR / r) else 0.0f

        val angle = atan2(adjustedY, adjustedX)
        return if (angle < 0) (angle + 2 * PI).toFloat() else angle
    }

    fun alignAndConvert(points: List<Coordinate>): List<Float> {
        // 1. Center the data
        val meanX = points.map { it.x }.average().toFloat()
        val meanY = points.map { it.y }.average().toFloat()
        val centered = points.map { (x, y) -> (x - meanX) to (y - meanY) }

        // 2. Find principal component (direction of maximum variance)
        val covariance = centered.map { (x, y) -> x * y }.sum() / points.size
        val varX = centered.map { (x, _) -> x * x }.sum() / points.size
        val varY = centered.map { (_, y) -> y * y }.sum() / points.size

        val pc1Angle = 0.5f * atan2(2 * covariance, varX - varY)

        // 3. Rotate to align PC1 with angle 0
        return centered.map { (x, y) ->
            val rotatedX = x * cos(-pc1Angle) - y * sin(-pc1Angle)
            val rotatedY = x * sin(-pc1Angle) + y * cos(-pc1Angle)

            val angle = atan2(rotatedY, rotatedX)
            if (angle < 0f) (angle + 2f * PI).toFloat() else angle
        }
    }

    private fun reduceDimensions(
        embeddings: Map<Int, DoubleArray>
    ): Map<Int, Coordinate> {
        val partitions = embeddings.keys.toList()
        val matrix = partitions.map { embeddings[it]!! }.toTypedArray()

        val coordinates = umap(
            data = matrix,
            d = 2,
            k = config.umapK,
            epochs = config.umapIterations,
        ).map {
            Coordinate(it[0].toFloat(), it[1].toFloat())
        }

        return partitions.zip(coordinates).toMap()
    }

    override fun getCircleRadius(): Float {
        return config.layerSpace
    }

    override fun getMaxDepth(): Int {
        return maxDepth
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
        return rTree
            .search(
                Geometries.rectangle(
                    rectangle.topLeft.x,
                    rectangle.topLeft.y,
                    rectangle.bottomRight.x,
                    rectangle.bottomRight.y,
                ),
            ).map(Entry<NodePlacement, PointFloat>::value)

    }

    override fun iterator(): Iterator<NodePlacement> = nodePlacementByState.values.iterator()
}