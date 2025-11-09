package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import com.github.davidmoten.rtree2.Entry
import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.ClusterLayout
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RTreeBuilder
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RTreeBuilder.toRTreeRectangle
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import smile.clustering.DBSCAN
import smile.manifold.umap
import smile.math.MathEx

class PartitionClusteringLayout(
    private val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val config: LayoutConfig.PartitionClusteringLayoutConfig = LayoutConfig.PartitionClusteringLayoutConfig()
) : ClusterLayout {

    private var isBuiltFlag = false

    private val coordinateByState = mutableMapOf<State, Coordinate>()
    private lateinit var rTree: RTree<NodePlacement, PointFloat>
    lateinit var boundingBoxByCluster: Map<Int, List<Coordinate>>

    override fun build(progressCallback: EpaProgressCallback?) {
        MathEx.setSeed(42);
        val embedder = PartitionFeatureEmbedder(
            extendedPrefixAutomaton = extendedPrefixAutomaton,
            config = PartitionEmbedderConfig.from(config),
            progressCallback = progressCallback
        )

        progressCallback?.onProgress(0, 2, "Create Embeddings")
        val featureEmbeddings = embedder.computeEmbedding()

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

        boundingBoxByCluster = createClusterPolygons(coordinates)

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

    private fun createClusterPolygons(coordinates: Map<State, Coordinate>): Map<Int, List<Coordinate>> {
        val coordinates2d = coordinates.map { (_, coordinate) ->
            arrayOf(coordinate.x.toDouble(), coordinate.y.toDouble()).toDoubleArray()
        }.toTypedArray()

        val dbscan = DBSCAN.fit(
            coordinates2d,
            3,
            50.0
        )

        val clusterLabels = dbscan.group()

        // Group points by cluster (excluding noise points with label -1)
        val pointsByCluster: Map<Int, Array<DoubleArray>> = coordinates2d.indices
            .filter { clusterLabels[it] >= 0 } // Exclude noise
            .groupBy { clusterLabels[it] }
            .mapValues { (_, indices) ->
                indices.map { coordinates2d[it] }.toTypedArray()
            }

        val geometryFactory = GeometryFactory()

        return pointsByCluster
            .mapValues { (_, points: Array<DoubleArray>) ->
                when {
                    points.size < 3 -> emptyList()
                    points.size == 3 -> {
                        points.map {
                            Coordinate(
                                x = it[0].toFloat(),
                                y = it[1].toFloat()
                            )
                        }
                    }

                    else -> {
                        val jtsCoordinates =
                            points.map { org.locationtech.jts.geom.Coordinate(it[0], it[1]) }.toTypedArray()

                        val hull = ConvexHull(jtsCoordinates, geometryFactory)
                        val basePolygon = hull.convexHull as? Polygon
                        val paddedPolygon = basePolygon?.buffer(35.0) as Polygon?

                        paddedPolygon?.coordinates?.map { coord ->
                            Coordinate(
                                x = coord.x.toFloat(),
                                y = coord.y.toFloat()
                            )
                        } ?: emptyList()
                    }
                }
            }.filter { it.value.isNotEmpty() }
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

        val usableWidth = config.canvasWidth
        val usableHeight = config.canvasHeight

        return partitions.zip(coords).associate { (state, coord) ->
            val x = if (rangeX > 0) {
                ((coord[0].toFloat() - minX) / rangeX) * usableWidth
            } else {
                config.canvasWidth / 2
            }

            val y = if (rangeY > 0) {
                ((coord[1].toFloat() - minY) / rangeY) * usableHeight
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

    override fun getClusterPolygons(): Map<Int, List<Coordinate>> {
        return boundingBoxByCluster
    }

}