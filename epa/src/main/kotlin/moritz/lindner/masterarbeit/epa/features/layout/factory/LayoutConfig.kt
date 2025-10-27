package moritz.lindner.masterarbeit.epa.features.layout.factory

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.ReductionMethod

sealed class LayoutConfig(val name: String) {

    abstract val enabled: Boolean

    abstract fun getParameters(): Map<String, ParameterInfo>
    abstract fun updateParameter(name: String, value: Any): LayoutConfig

    data class WalkerConfig(
        val distance: Float = 200.0f,
        val layerSpace: Float = 200.0f,
        override val enabled: Boolean = true,
    ) : LayoutConfig("Walker") {
        override fun getParameters() = mapOf(
            "distance" to ParameterInfo.NumberParameterInfo("Distance", 1f, 500.0f, 5.0f),
            "layerSpace" to ParameterInfo.NumberParameterInfo("LayerSpace", 1.0f, 500.0f, 5.0f),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled")
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "distance" -> copy(distance = value as Float)
            "layerSpace" -> copy(layerSpace = value as Float)
            "enabled" -> copy(enabled = value as Boolean)
            else -> this
        }
    }

    data class TimeRadialWalkerConfig(
        val layerBaseUnit: Float = 500.0f,
        val margin: Float = 5.0f,
        val rotation: Float = 90.0f,
        val minCycleTimeDifference: Float = 0.0f,
        val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        override val enabled: Boolean = true,
    ) : LayoutConfig("Radial Walker Time") {
        override fun getParameters() = mapOf(
            "layerBaseUnit" to ParameterInfo.NumberParameterInfo("layerBaseUnit", 1.0f, 2000.0f, 10f),
            "margin" to ParameterInfo.NumberParameterInfo("Margin (in Degrees)", 0.0f, 360.0f, 0.1f),
            "rotation" to ParameterInfo.NumberParameterInfo("Rotation", 0.0f, 360.0f, 1.0f),
            "minCycleTimeDifference" to ParameterInfo.NumberParameterInfo("Min Cycletime change", 0.0f, 1.0f, .1f),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled")
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "layerBaseUnit" -> copy(layerBaseUnit = value as Float)
            "margin" -> copy(margin = value as Float)
            "rotation" -> copy(rotation = value as Float)
            "minCycleTimeDifference" -> copy(minCycleTimeDifference = value as Float)
            "enabled" -> copy(enabled = value as Boolean)
            else -> this
        }
    }

    data class RadialWalkerConfig(
        val layerSpace: Float = 120.0f,
        val margin: Float = 5.0f,
        val rotation: Float = 90.0f,
        override val enabled: Boolean = true,
    ) : LayoutConfig("Radial Walker") {
        override fun getParameters() = mapOf(
            "layerSpace" to ParameterInfo.NumberParameterInfo("Layer Space", 10.0f, 300.0f, 5.0f),
            "margin" to ParameterInfo.NumberParameterInfo("Margin (in Degrees)", 0.0f, 360.0f, 0.1f),
            "rotation" to ParameterInfo.NumberParameterInfo("Rotation", 0.0f, 360.0f, 1.0f),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled")
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "layerSpace" -> copy(layerSpace = value as Float)
            "margin" -> copy(margin = value as Float)
            "rotation" -> copy(rotation = value as Float)
            "enabled" -> copy(enabled = value as Boolean)
            else -> this
        }
    }

    data class PartitionSimilarityRadialLayoutConfig(
        override val enabled: Boolean = true,
        val layerSpace: Float = 120.0f,
        val umapK: Int = 10,
        val umapIterations: Int = 250,
    ) : LayoutConfig("Partition Similarity Radial") {
        override fun getParameters(): Map<String, ParameterInfo> {
            return mapOf(
                "umapK" to ParameterInfo.NumberParameterInfo("UMAP K", 2, 50, 1),
                "umapIterations" to ParameterInfo.NumberParameterInfo("UMAP Iterations", 50, 500, 50),
                "layerSpace" to ParameterInfo.NumberParameterInfo("LayerSpace", 1.0f, 500.0f, 5.0f),
                "enabled" to ParameterInfo.BooleanParameterInfo("Enabled")
            )
        }

        override fun updateParameter(
            name: String,
            value: Any
        ): LayoutConfig {
            return when (name) {
                "umapK" -> copy(umapK = value as Int)
                "umapIterations" -> copy(umapIterations = value as Int)
                "layerSpace" -> copy(layerSpace = value as Float)
                "enabled" -> copy(enabled = value as Boolean)
                else -> this
            }
        }
    }

    data class DirectAngularConfig(
        val layerSpace: Float = 50.0f,
        val rotation: Float = 0.0f,
        override val enabled: Boolean = true,
    ) : LayoutConfig("Direct Angular") {
        override fun getParameters() = mapOf(
            "layerSpace" to ParameterInfo.NumberParameterInfo("Layer Space", 10.0f, 200.0f, 5.0f),
            "rotation" to ParameterInfo.NumberParameterInfo("Rotation", 0.0f, 360.0f, 1.0f),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled")
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "layerSpace" -> copy(layerSpace = value as Float)
            "rotation" -> copy(rotation = value as Float)
            "enabled" -> copy(enabled = value as Boolean)
            else -> this
        }
    }

    data class StateClusteringLayoutConfig(
        // Graph embedding parameters
        val useGraphEmbedding: Boolean = false,
        val graphEmbeddingDims: Int = 16,
        val walkLength: Int = 10,
        val walksPerVertex: Int = 10,
        val windowSize: Int = 5,

        // Feature embedding parameters
        val useFeatureEmbedding: Boolean = true,
        val featureEmbeddingDims: Int = 16,

        // Feature flags
        val useDepthFeature: Boolean = true,
        val useOutgoingTransitions: Boolean = true,
        val usePartitionValue: Boolean = true,
        val useSequenceLength: Boolean = true,
        val useCycleTime: Boolean = true,
        val usePathLength: Boolean = true,
        val useActivity: Boolean = true,

        // Reduction parameters
        val reductionMethod: ReductionMethod = ReductionMethod.UMAP,
        val umapK: Int = 10,
        val iterations: Int = 300,

        // Layout parameters
        val canvasWidth: Float = 2000.0f,
        val canvasHeight: Float = 2000.0f,
        val nodeRadius: Float = 5.0f,
        val padding: Float = 50.0f,

        // Force-directed parameters
        val useForceDirected: Boolean = false,
        val repulsionStrength: Float = 100.0f,
        val forceDirectedLayoutIterations: Int = 10,
        val useResolveOverlap: Boolean = false,
        override val enabled: Boolean = true,
    ) : LayoutConfig("State-Clustering Layout") {

        override fun getParameters() = mapOf(
            // Graph embedding
            "useGraphEmbedding" to ParameterInfo.BooleanParameterInfo("Use Graph Embedding"),
            "graphEmbeddingDims" to ParameterInfo.NumberParameterInfo("Graph Embedding Dims", 2, 128, 2),
            "walkLength" to ParameterInfo.NumberParameterInfo("Walk Length", 5, 50, 5),
            "walksPerVertex" to ParameterInfo.NumberParameterInfo("Walks Per Vertex", 5, 50, 5),
            "windowSize" to ParameterInfo.NumberParameterInfo("Window Size", 3, 20, 1),

            // Feature embedding
            "useFeatureEmbedding" to ParameterInfo.BooleanParameterInfo("Use Feature Embedding"),
            "featureEmbeddingDims" to ParameterInfo.NumberParameterInfo("Feature Embedding Dims", 2, 128, 2),

            // Feature flags
            "useDepthFeature" to ParameterInfo.BooleanParameterInfo("Use Depth Feature"),
            "useOutgoingTransitions" to ParameterInfo.BooleanParameterInfo("Use Outgoing Transitions"),
            "usePartitionValue" to ParameterInfo.BooleanParameterInfo("Use Partition Value"),
            "useSequenceLength" to ParameterInfo.BooleanParameterInfo("Use Sequence Length"),
            "useCycleTime" to ParameterInfo.BooleanParameterInfo("Use Cycle Time"),
            "usePathLength" to ParameterInfo.BooleanParameterInfo("Use Path Length"),
            "useActivity" to ParameterInfo.BooleanParameterInfo("Use Activity"),

            // Reduction parameters
            "reductionMethod" to ParameterInfo.EnumParameterInfo("Reduction Method", ReductionMethod.entries),
            "umapK" to ParameterInfo.NumberParameterInfo("UMAP K", 2, 50, 1),
            "Iterations" to ParameterInfo.NumberParameterInfo("Iterations", 0, 500, 1),

            // Layout parameters
            "canvasWidth" to ParameterInfo.NumberParameterInfo("Canvas Width", 500.0f, 5000.0f, 100.0f),
            "canvasHeight" to ParameterInfo.NumberParameterInfo("Canvas Height", 500.0f, 5000.0f, 100.0f),
            "nodeRadius" to ParameterInfo.NumberParameterInfo("Node Radius", 1.0f, 20.0f, 1.0f),
            "padding" to ParameterInfo.NumberParameterInfo("Padding", 10.0f, 200.0f, 10.0f),

            // Force-directed parameters
            "useForceDirected" to ParameterInfo.BooleanParameterInfo("Use Force Directed"),
            "repulsionStrength" to ParameterInfo.NumberParameterInfo("Repulsion Strength", 10.0f, 500.0f, 10.0f),
            "forceDirectedLayoutIterations" to ParameterInfo.NumberParameterInfo(
                "Force Directed Iterations",
                5,
                100,
                5
            ),

            "useResolveOverlap" to ParameterInfo.BooleanParameterInfo("Use Resolve Overlap"),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled")
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "useGraphEmbedding" -> copy(useGraphEmbedding = value as Boolean)
            "graphEmbeddingDims" -> copy(graphEmbeddingDims = value as Int)
            "walkLength" -> copy(walkLength = value as Int)
            "walksPerVertex" -> copy(walksPerVertex = value as Int)
            "windowSize" -> copy(windowSize = value as Int)

            "useFeatureEmbedding" -> copy(useFeatureEmbedding = value as Boolean)
            "featureEmbeddingDims" -> copy(featureEmbeddingDims = value as Int)

            "useDepthFeature" -> copy(useDepthFeature = value as Boolean)
            "useOutgoingTransitions" -> copy(useOutgoingTransitions = value as Boolean)
            "usePartitionValue" -> copy(usePartitionValue = value as Boolean)
            "useSequenceLength" -> copy(useSequenceLength = value as Boolean)
            "useCycleTime" -> copy(useCycleTime = value as Boolean)
            "usePathLength" -> copy(usePathLength = value as Boolean)
            "useActivity" -> copy(useActivity = value as Boolean)

            "reductionMethod" -> copy(reductionMethod = value as ReductionMethod)
            "umapK" -> copy(umapK = value as Int)
            "Iterations" -> copy(iterations = value as Int)

            "canvasWidth" -> copy(canvasWidth = value as Float)
            "canvasHeight" -> copy(canvasHeight = value as Float)
            "nodeRadius" -> copy(nodeRadius = value as Float)
            "padding" -> copy(padding = value as Float)

            "useForceDirected" -> copy(useForceDirected = value as Boolean)
            "repulsionStrength" -> copy(repulsionStrength = value as Float)
            "forceDirectedLayoutIterations" -> copy(forceDirectedLayoutIterations = value as Int)

            "useResolveOverlap" -> copy(useResolveOverlap = value as Boolean)
            "enabled" -> copy(enabled = value as Boolean)
            else -> this
        }
    }

    data class PartitionClusteringLayoutConfig(
        override val enabled: Boolean = true,
        val umapK: Int = 10,
        val umapIterations: Int = 250,
        val canvasWidth: Float = 2000.0f,
        val canvasHeight: Float = 2000.0f,
        val nodeRadius: Float = 5.0f,
        val padding: Float = 50.0f,
    ) : LayoutConfig("Partition-Clustering Layout") {

        override fun getParameters() = mapOf(
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled"),
            "umapK" to ParameterInfo.NumberParameterInfo("UMAP K", 2, 50, 1),
            "umapIterations" to ParameterInfo.NumberParameterInfo("UMAP Iterations", 50, 500, 50),
            "canvasWidth" to ParameterInfo.NumberParameterInfo("Canvas Width", 500.0f, 5000.0f, 100.0f),
            "canvasHeight" to ParameterInfo.NumberParameterInfo("Canvas Height", 500.0f, 5000.0f, 100.0f),
            "nodeRadius" to ParameterInfo.NumberParameterInfo("Node Radius", 1.0f, 20.0f, 1.0f),
            "padding" to ParameterInfo.NumberParameterInfo("Padding", 10.0f, 200.0f, 10.0f),
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "enabled" -> copy(enabled = value as Boolean)
            "umapK" -> copy(umapK = value as Int)
            "umapIterations" -> copy(umapIterations = value as Int)
            "canvasWidth" -> copy(canvasWidth = value as Float)
            "canvasHeight" -> copy(canvasHeight = value as Float)
            "nodeRadius" -> copy(nodeRadius = value as Float)
            "padding" -> copy(padding = value as Float)
            else -> this
        }
    }

    enum class PRTInitialLayout {
        Compact,
        EdgeLength
    }

    data class PRTLayoutConfig(
        override val enabled: Boolean = true,
        val initializer: PRTInitialLayout = PRTInitialLayout.Compact,
        val labelSizeByState: Map<State, Pair<Float, Float>>,
        val iterations: Int = 10,
        val seed: Int = 42
    ) : LayoutConfig("PRT") {
        override fun getParameters(): Map<String, ParameterInfo> {
            return mapOf(
                "enabled" to ParameterInfo.BooleanParameterInfo("Enabled"),
                "initialization" to ParameterInfo.EnumParameterInfo(
                    "initialization",
                    PRTInitialLayout.entries
                ),
                "iterations" to ParameterInfo.NumberParameterInfo(
                    name = "iterations",
                    min = 0,
                    max = 100,
                    step = 1
                )
            )
        }

        override fun updateParameter(
            name: String,
            value: Any
        ) = when (name) {
            "enabled" -> copy(enabled = value as Boolean)
            "initialization" -> copy(initializer = value as PRTInitialLayout)
            "iterations" -> copy(iterations = value as Int)
            else -> this
        }
    }
}