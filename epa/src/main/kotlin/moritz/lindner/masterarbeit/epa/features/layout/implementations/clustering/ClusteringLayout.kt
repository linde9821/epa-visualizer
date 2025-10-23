package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import com.github.davidmoten.rtree2.Entry
import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RTreeBuilder
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import moritz.lindner.masterarbeit.epa.features.layout.placement.Vector2D
import smile.manifold.umap
import kotlin.math.sqrt

class ClusteringLayout(
    private val epa: ExtendedPrefixAutomaton<Long>,
    private val config: LayoutConfig.ClusteringLayoutConfig = LayoutConfig.ClusteringLayoutConfig()
) : Layout {

    private val logger = KotlinLogging.logger { }
    private var isBuiltFlag = false
    private val nodeCoordinates = mutableMapOf<State, Coordinate>()

    private lateinit var rTree: RTree<NodePlacement, PointFloat>

    override fun build(
        progressCallback: EpaProgressCallback?
    ) {
        logger.info { "building $config" }
        progressCallback?.onProgress(0, 7, "Starting semantic layout...")

        progressCallback?.onProgress(1, 7, "Creating graph embeddings...")
        val graphEmbeddings = createGraphEmbeddings(progressCallback)

        val featureEmbeddings = createFeatureEmbeddings(progressCallback)

        progressCallback?.onProgress(3, 7, "Combining embeddings...")
        val combinedEmbeddings = combineEmbeddings(graphEmbeddings, featureEmbeddings)

        progressCallback?.onProgress(4, 7, "Reducing dimensions...")
        val coordinates2D = reduceDimensions(combinedEmbeddings)

        progressCallback?.onProgress(6, 7, "Resolving conflicts...")
        val finalCoordinates = resolveConflicts(coordinates2D, progressCallback)

        progressCallback?.onProgress(7, 7, "Finalizing layout...")
        finalizeLayout(finalCoordinates)

        rTree = RTreeBuilder.build(nodeCoordinates.map {
            NodePlacement(
                coordinate = it.value,
                state = it.key
            )
        })
        isBuiltFlag = true
    }

    override fun getCoordinate(state: State): Coordinate {
        check(isBuiltFlag) { "Layout not built yet" }
        return nodeCoordinates[state]
            ?: throw NoSuchElementException("State not found: $state")
    }

    override fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacement> {
        check(isBuiltFlag) { "Layout not built yet" }
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

        // Check minimum requirements
        if (states.size < 3) {
            // Too few states for dimensionality reduction
//            return handleSmallDataset(states, matrix)
        }

        // TODO: add others
        val coordinates2D = when (config.reductionMethod) {
            ReductionMethod.UMAP -> computeUMAP(matrix)
        }

        return scaleToCanvas(states, coordinates2D)
    }

    private fun computeUMAP(matrix: Array<DoubleArray>): Array<DoubleArray> {
        return umap(
            data = matrix,
            d = 2,
            k = config.umapK,
            epochs = config.umapIterations,
        )
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

        val usableWidth = config.canvasWidth - 2 * config.padding
        val usableHeight = config.canvasHeight - 2 * config.padding

        return states.zip(coords).associate { (state, coord) ->
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

    private fun resolveConflicts(
        coordinates: Map<State, Coordinate>,
        progressCallback: EpaProgressCallback?,
    ): Map<State, Coordinate> {
        var result = coordinates

        if (config.useForceDirected) {
            result = applyForceDirectedLayout(result, progressCallback)
        }

        if (config.useResolveOverlap) {
            result = resolveOverlaps(result, progressCallback)
        }

        return result
    }

    private fun applyForceDirectedLayout(
        coordinates: Map<State, Coordinate>,
        progressCallback: EpaProgressCallback?,
    ): Map<State, Coordinate> {
        val positions = coordinates.toMutableMap()
        val states = positions.keys.toList()

        repeat(config.forceDirectedLayoutIterations) {
            progressCallback?.onProgress(it, config.forceDirectedLayoutIterations, "apply force directed layout")
            val forces = mutableMapOf<State, Vector2D>()

            // Calculate repulsion forces
            states.forEach { s1 ->
                var force = Vector2D(0.0f, 0.0f)

                states.forEach { s2 ->
                    if (s1 != s2) {
                        val pos1 = positions[s1]!!
                        val pos2 = positions[s2]!!
                        val diff = pos1.vectorTo(pos2)
                        val dist = diff.magnitude()

                        if (dist > 0 && dist < config.repulsionStrength * 2) {
                            // Repulsion force
                            val repulsion = diff.normalize().multiply(
                                -config.repulsionStrength / (dist * dist)
                            )

                            // Stronger repulsion between different clusters
                            val clusterMultiplier = 1.0f
//                            val clusterMultiplier = if (clusters[s1] != clusters[s2]) {
//                                2.0f
//                            } else {
//                                1.0f
//                            }

                            force = force.add(repulsion.multiply(clusterMultiplier))
                        }
                    }
                }

                forces[s1] = force
            }

            // Apply forces with damping
            val damping = 0.1f
            forces.forEach { (state, force) ->
                val pos = positions[state]!!
                positions[state] = Coordinate(
                    pos.x + force.x * damping,
                    pos.y + force.y * damping
                )
            }
        }

        return positions
    }

    private fun resolveOverlaps(
        coordinates: Map<State, Coordinate>,
        progressCallback: EpaProgressCallback?
    ): Map<State, Coordinate> {
        val positions = coordinates.toMutableMap()
        val states = positions.keys.toList()
        val minDistance = config.nodeRadius * 2.5f

        // Simple overlap resolution
        var hasOverlap = true
        var iterations = 0

        val totalIterations = 20
        while (hasOverlap && iterations < totalIterations) {
            progressCallback?.onProgress(iterations, totalIterations, "Resolve overlap")
            hasOverlap = false

            states.forEach { s1 ->
                states.forEach { s2 ->
                    if (s1 != s2) {
                        val pos1 = positions[s1]!!
                        val pos2 = positions[s2]!!
                        val dist = pos1.distanceTo(pos2)

                        if (dist < minDistance) {
                            hasOverlap = true
                            val push = (minDistance - dist) / 2.0f
                            val direction = pos1.vectorTo(pos2).normalize()

                            positions[s1] = pos1.move(direction.multiply(-push))
                            positions[s2] = pos2.move(direction.multiply(push))
                        }
                    }
                }
            }
            iterations++
        }

        return positions
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
