package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import com.github.davidmoten.rtree2.Entry
import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
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

class PartitionClusteringLayout(
    private val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val config: LayoutConfig.PartitionClusteringLayoutConfig = LayoutConfig.PartitionClusteringLayoutConfig()
) : Layout {

    private var isBuiltFlag = false

    private val coordinateByState = mutableMapOf<State, Coordinate>()
    private lateinit var rTree: RTree<NodePlacement, PointFloat>

    override fun build(progressCallback: EpaProgressCallback?) {
        MathEx.setSeed(42);
        val embedder = PartitionFeatureEmbedder()

        progressCallback?.onProgress(0, 2, "Create Embeddings")
        val featureEmbeddings = embedder.computeEmbedding(extendedPrefixAutomaton)

        progressCallback?.onProgress(1, 2, "Reduce Dimensions")
        val partitionCoordinates2D = reduceDimensions(featureEmbeddings)

        val coordinates = extendedPrefixAutomaton.states.associateWith { state ->
            val partition = extendedPrefixAutomaton.partition(state)
            partitionCoordinates2D[partition]!!
        }

        rTree = RTreeBuilder.build(coordinates.map {
            NodePlacement(
                coordinate = it.value,
                state = it.key
            )
        })

        coordinates.forEach { (state, coordinate) ->
            coordinateByState[state] = coordinate
        }

        isBuiltFlag = true
    }

    private fun reduceDimensions(
        embeddings: Map<Int, DoubleArray>
    ): Map<Int, Coordinate> {
        val partitions = embeddings.keys.toList()
        val matrix = partitions.map { embeddings[it]!! }.toTypedArray()

        val coordinates2D = umap(
            data = matrix,
            d = 2,
            k = config.umapK,
            epochs = config.umapIterations,
        )

        return scaleToCanvas(partitions, coordinates2D)
    }

    private fun scaleToCanvas(
        partitions: List<Int>,
        coords: Array<DoubleArray>
    ): Map<Int, Coordinate> {
        // Find bounds
        val minX = coords.minOf { it[0] }.toFloat()
        val maxX = coords.maxOf { it[0] }.toFloat()
        val minY = coords.minOf { it[1] }.toFloat()
        val maxY = coords.maxOf { it[1] }.toFloat()

        val rangeX = maxX - minX
        val rangeY = maxY - minY

        val usableWidth = config.canvasWidth - 2 * config.padding
        val usableHeight = config.canvasHeight - 2 * config.padding

        return partitions.zip(coords).associate { (state, coord) ->
            val x = if (rangeX > 0) {
                ((coord[0].toFloat() - minX) / rangeX) * usableWidth + config.padding
            } else {
                config.canvasWidth / 2
            }

            val y = if (rangeY > 0) {
                ((coord[1].toFloat() - minY) / rangeY) * usableHeight + config.padding
            } else {
                config.canvasHeight / 2
            }

            state to Coordinate(x, y)
        }
    }


    override fun isBuilt(): Boolean {
        return isBuiltFlag
    }

    override fun getCoordinate(state: State): Coordinate {
        check(isBuiltFlag) { "Layout not built yet" }
        return coordinateByState[state]
            ?: throw NoSuchElementException("State not found: $state")
    }

    override fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacement> {
        check(isBuiltFlag) { "Layout not built yet" }
        return rTree
            .search(rectangle.toRTreeRectangle()).map(Entry<NodePlacement, PointFloat>::value)
    }

    override fun iterator(): Iterator<NodePlacement> {
        check(isBuiltFlag) { "Layout not built yet" }
        return coordinateByState.entries
            .map { (state, coordinate) ->
                NodePlacement(coordinate, state)
            }
            .iterator()
    }

}