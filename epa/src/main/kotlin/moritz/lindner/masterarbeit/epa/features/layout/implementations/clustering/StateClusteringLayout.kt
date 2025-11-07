package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import com.github.davidmoten.rtree2.Entry
import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RTreeBuilder
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RTreeBuilder.toRTreeRectangle
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import smile.manifold.umap
import smile.math.MathEx
import kotlin.math.sqrt

class StateClusteringLayout(
    private val epa: ExtendedPrefixAutomaton<Long>,
    private val config: LayoutConfig.StateClusteringLayoutConfig = LayoutConfig.StateClusteringLayoutConfig()
) : Layout {

    private val logger = KotlinLogging.logger { }
    private var isBuiltFlag = false
    private val nodeCoordinates = mutableMapOf<State, Coordinate>()

    private lateinit var rTree: RTree<NodePlacement, PointFloat>

    override fun build(
        progressCallback: EpaProgressCallback?
    ) {
        MathEx.setSeed(42);
        progressCallback?.onProgress(0, 4, "Creating graph embeddings...")
        val graphEmbeddings = createGraphEmbeddings(progressCallback)

        progressCallback?.onProgress(1, 4, "Create feature embeddings...")
        val featureEmbeddings = createFeatureEmbeddings(progressCallback)

        val combinedEmbeddings = combineEmbeddings(graphEmbeddings, featureEmbeddings)

        progressCallback?.onProgress(2, 4, "Reducing dimensions...")
        val coordinates = reduceDimensions(combinedEmbeddings)

        finalizeLayout(coordinates)

        rTree = RTreeBuilder.build(
            nodePlacements = nodeCoordinates.map {
                NodePlacement(
                    coordinate = it.value,
                    state = it.key
                )
            },
            epaProgressCallback = progressCallback
        )
        isBuiltFlag = true
    }

    override fun getCoordinate(state: State): Coordinate {
        check(isBuiltFlag) { "Layout not built yet" }
        return nodeCoordinates[state]
            ?: throw NoSuchElementException("State not found: $state")
    }

    override fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacement> {
        check(isBuiltFlag) { "Layout not built yet" }
        return rTree.search(rectangle.toRTreeRectangle()).map(Entry<NodePlacement, PointFloat>::value)
    }

    override fun isBuilt(): Boolean = isBuiltFlag

    override fun iterator(): Iterator<NodePlacement> {
        check(isBuiltFlag) { "Layout not built yet" }
        return nodeCoordinates.entries
            .map { (state, coordinate) ->
                NodePlacement(coordinate, state)
            }
            .iterator()
    }

    private fun createGraphEmbeddings(progressCallback: EpaProgressCallback?): Map<State, DoubleArray> {
        return if (config.useGraphEmbedding) {
            val embedder = GraphEmbedder(epa, config, progressCallback)
            embedder.computeEmbeddings()
        } else emptyMap()
    }

    private fun createFeatureEmbeddings(progressCallback: EpaProgressCallback?): Map<State, DoubleArray> {
        return if (config.useFeatureEmbedding) {
            val embedder = StateFeatureEmbedder(epa, config, progressCallback)
            embedder.computeEmbeddings()
        } else emptyMap()
    }

    private fun combineEmbeddings(
        graphEmbeddings: Map<State, DoubleArray>,
        featureEmbeddings: Map<State, DoubleArray>
    ): Map<State, DoubleArray> {
        return epa.states.associateWith { state ->
            val graphEmb = graphEmbeddings[state] ?: DoubleArray(config.graphEmbeddingDims)
            val featureEmb = featureEmbeddings[state] ?: DoubleArray(config.featureEmbeddingDims)

            val normalizedGraph = normalize(graphEmb)
            val normalizedFeature = normalize(featureEmb)

            if (config.useGraphEmbedding && config.useFeatureEmbedding) {
                normalizedGraph + normalizedFeature
            } else if (config.useFeatureEmbedding) {
                normalizedFeature
            } else {
                normalizedGraph
            }
        }
    }

    private fun normalize(vector: DoubleArray): DoubleArray {
        val magnitude = sqrt(vector.sumOf { it * it })
        return if (magnitude > 0) {
            vector.map { it / magnitude }.toDoubleArray()
        } else vector
    }

    private fun reduceDimensions(
        embeddings: Map<State, DoubleArray>
    ): Map<State, Coordinate> {
        val states = embeddings.keys.toList()
        val matrix = states.map { embeddings[it]!! }.toTypedArray()

        val coordinates2D = umap(
            data = matrix,
            d = 2,
            k = config.umapK,
            epochs = config.iterations,
        )

        return scaleToCanvas(states, coordinates2D)
    }

    private fun scaleToCanvas(
        states: List<State>,
        coords: Array<DoubleArray>
    ): Map<State, Coordinate> {
        // Find bounds
        val minX = coords.minOf { it[0] }.toFloat()
        val maxX = coords.maxOf { it[0] }.toFloat()
        val minY = coords.minOf { it[1] }.toFloat()
        val maxY = coords.maxOf { it[1] }.toFloat()

        val rangeX = maxX - minX
        val rangeY = maxY - minY

        return states.zip(coords).associate { (state, coord) ->
            val x = if (rangeX > 0) {
                ((coord[0].toFloat() - minX) / rangeX) * config.canvasWidth
            } else {
                config.canvasWidth / 2
            }

            val y = if (rangeY > 0) {
                ((coord[1].toFloat() - minY) / rangeY) * config.canvasHeight
            } else {
                config.canvasHeight / 2
            }

            state to Coordinate(x, y)
        }
    }


    private fun finalizeLayout(
        coordinates: Map<State, Coordinate>,
//        clusters: Map<State, Int>
    ) {
        // Store coordinates
        nodeCoordinates.clear()
        nodeCoordinates.putAll(coordinates)

        // Store clusters
//        nodeClusters.clear()
//        nodeClusters.putAll(clusters)

        // Calculate cluster bounding boxes
//        if (clusters.isNotEmpty()) {
//            calculateClusterBounds(coordinates, clusters)
//        }
    }
}

