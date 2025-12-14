package moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.semantic

import com.github.davidmoten.rtree2.Entry
import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.RadialTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RTreeBuilder
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RTreeBuilder.toRTreeRectangle
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.PartitionEmbedderConfig
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.PartitionFeatureEmbedder
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import smile.manifold.umap
import smile.math.MathEx
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max

class PartitionSimilarityRadialLayout(
    private val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val config: LayoutConfig.PartitionSimilarityRadialLayoutConfig = LayoutConfig.PartitionSimilarityRadialLayoutConfig(),
    private val backgroundDispatcher: ExecutorCoroutineDispatcher
) : RadialTreeLayout {

    private val logger = KotlinLogging.logger {  }
    private val epaService = EpaService<Long>()
    private var isBuiltFlag = false
    private val nodePlacementByState = HashMap<State, NodePlacement>(extendedPrefixAutomaton.states.size)
    private lateinit var rTree: RTree<NodePlacement, PointFloat>

    private var maxDepth = Int.MIN_VALUE

    override fun build(progressCallback: EpaProgressCallback?) {
        MathEx.setSeed(42);

        val embedder = PartitionFeatureEmbedder(
            extendedPrefixAutomaton = extendedPrefixAutomaton,
            config = PartitionEmbedderConfig.from(config),
            progressCallback = progressCallback,
            backgroundDispatcher = backgroundDispatcher
        )

        progressCallback?.onProgress(0, 2, "Create Embedding")
        val featureEmbeddings = embedder.computeEmbedding()

        progressCallback?.onProgress(1, 2, "Reduce Dimensions")
        val partitionCoordinates = reduceDimensions(featureEmbeddings)
        val angleByPartition = partitionCoordinates.values.toList().map { toAngle(it) }.toList()
        val partitionToAngleMap = partitionCoordinates.keys.zip(angleByPartition).toMap()

        val subtreeSizes = epaService.subtreeSizeByState(extendedPrefixAutomaton)

        placeNodesHierarchically(
            subtreeSizes = subtreeSizes,
            partitionToAngleMap = partitionToAngleMap
        )

        rTree = RTreeBuilder.build(nodePlacementByState.values.toList(), progressCallback)
        isBuiltFlag = true
    }

    fun toAngle(coordinate: Coordinate): Float {
        val angle = atan2(coordinate.y, coordinate.x)
        return if (angle < 0) (angle + 2 * PI).toFloat() else angle
    }

    private fun placeNodesHierarchically(
        subtreeSizes: Map<State, Int>,
        partitionToAngleMap: Map<Int, Float>
    ) {
        placeNode(
            state = State.Root,
            depth = 0,
            wedgeStart = 0f,
            wedgeEnd = (2 * PI).toFloat(),
            subtreeSizes = subtreeSizes,
            partitionToAngleMap = partitionToAngleMap
        )
    }

    private fun placeNode(
        state: State,
        depth: Int,
        wedgeStart: Float,
        wedgeEnd: Float,
        subtreeSizes: Map<State, Int>,
        partitionToAngleMap: Map<Int, Float>
    ) {
        maxDepth = max(maxDepth, depth)

        val partition = extendedPrefixAutomaton.partition(state)
        val preferredAngle = partitionToAngleMap[partition]!!

        val constrainedAngle = constrainAngleToWedge(preferredAngle, wedgeStart, wedgeEnd)

        nodePlacementByState[state] = NodePlacement(
            coordinate = Coordinate.fromPolar(depth * config.layerSpace, constrainedAngle),
            state = state
        )

        val children = extendedPrefixAutomaton.outgoingTransitionsByState[state]?.map {
            it.end
        }?.sortedByDescending { subtreeSizes[it] ?: 0 } ?: emptyList()

        if (children.isEmpty()) return

        val totalChildrenSize = children.sumOf { subtreeSizes[it] ?: 1 }
        val wedgeSize = wedgeEnd - wedgeStart

        var currentAngle = wedgeStart

        children.forEach { child ->
            val childSize = subtreeSizes[child] ?: 1
            val childWedgeSize = wedgeSize * (childSize.toFloat() / totalChildrenSize)
            val childWedgeEnd = currentAngle + childWedgeSize

            placeNode(
                state = child,
                depth = depth + 1,
                wedgeStart = currentAngle,
                wedgeEnd = childWedgeEnd,
                subtreeSizes = subtreeSizes,
                partitionToAngleMap = partitionToAngleMap
            )

            currentAngle = childWedgeEnd
        }
    }

    private fun constrainAngleToWedge(angle: Float, wedgeStart: Float, wedgeEnd: Float): Float {
        return angle.coerceIn(wedgeStart, wedgeEnd)
    }

    private fun reduceDimensions(
        embeddings: Map<Int, DoubleArray>
    ): Map<Int, Coordinate> {
        val partitions = embeddings.keys.toList()
        val matrix = partitions.map { embeddings[it]!! }.toTypedArray()

        if (embeddings.size <= config.umapK) {
            logger.warn { "Not enough data for UMAP reduction. Needed ${config.umapK} embeddings and ${embeddings.size} are available" }
            throw IllegalStateException("Not enough data for UMAP reduction. Needed ${config.umapK} embeddings and ${embeddings.size} are available")
        }

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
            "No coordinate for $state present",
        )
    }

    override fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacement> {
        return rTree.search(rectangle.toRTreeRectangle()).map(Entry<NodePlacement, PointFloat>::value)

    }

    override fun iterator(): Iterator<NodePlacement> = nodePlacementByState.values.iterator()
}
