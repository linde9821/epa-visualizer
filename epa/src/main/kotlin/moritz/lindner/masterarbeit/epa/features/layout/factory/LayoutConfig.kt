package moritz.lindner.masterarbeit.epa.features.layout.factory

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State

sealed class LayoutConfig(val name: String) {

    abstract val enabled: Boolean
    abstract val lod: Boolean

    abstract fun getParameters(): Map<String, ParameterInfo>
    abstract fun updateParameter(name: String, value: Any): LayoutConfig

    data class WalkerConfig(
        val distance: Float = 200.0f,
        val layerSpace: Float = 200.0f,
        override val enabled: Boolean = true,
        override val lod: Boolean = false
    ) : LayoutConfig("Walker Tree Layout") {
        override fun getParameters() = mapOf(
            "distance" to ParameterInfo.NumberParameterInfo("Distance", 1f, 500.0f, 5.0f),
            "layerSpace" to ParameterInfo.NumberParameterInfo("LayerSpace", 1.0f, 500.0f, 5.0f),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled"),
            "lod" to ParameterInfo.BooleanParameterInfo("Level of Detail")
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "distance" -> copy(distance = value as Float)
            "layerSpace" -> copy(layerSpace = value as Float)
            "enabled" -> copy(enabled = value as Boolean)
            "lod" -> copy(lod = value as Boolean)
            else -> this
        }
    }

    data class RadialWalkerConfig(
        val layerSpace: Float = 120.0f,
        val margin: Float = 5.0f,
        val rotation: Float = 90.0f,
        override val enabled: Boolean = true,
        override val lod: Boolean = false
    ) : LayoutConfig("Radial Walker Tree Layout") {
        override fun getParameters() = mapOf(
            "layerSpace" to ParameterInfo.NumberParameterInfo("Layer Space", 10.0f, 300.0f, 5.0f),
            "margin" to ParameterInfo.NumberParameterInfo("Margin (in Degrees)", 0.0f, 360.0f, 0.1f),
            "rotation" to ParameterInfo.NumberParameterInfo("Rotation", 0.0f, 360.0f, 1.0f),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled"),
            "lod" to ParameterInfo.BooleanParameterInfo("Level of Detail")
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "layerSpace" -> copy(layerSpace = value as Float)
            "margin" -> copy(margin = value as Float)
            "rotation" -> copy(rotation = value as Float)
            "enabled" -> copy(enabled = value as Boolean)
            "lod" -> copy(lod = value as Boolean)
            else -> this
        }
    }
    data class TimeBasedRadialConfig(
        val margin: Float = 5.0f,
        val rotation: Float = 0.0f,
        val minEdgeLength: Float = 10.0f,
        val maxEdgeLength: Float = 1000.0f,
        val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        override val enabled: Boolean = true,
        override val lod: Boolean = false
    ) : LayoutConfig("Time-based Radial Walker Tree Layout") {
        override fun getParameters() = mapOf(
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled"),
            "lod" to ParameterInfo.BooleanParameterInfo("Level of Detail"),
            "margin" to ParameterInfo.NumberParameterInfo("Margin (in Degrees)", 0.0f, 360.0f, 0.1f),
            "rotation" to ParameterInfo.NumberParameterInfo("Rotation", 0.0f, 360.0f, 1.0f),
            "minEdgeLength" to ParameterInfo.NumberParameterInfo(
                name = "minEdgeLength",
                min = 0f,
                max = 1000f,
                steps = 10f
            ),
            "maxEdgeLength" to ParameterInfo.NumberParameterInfo(
                name = "maxEdgeLength",
                min = 100f,
                max = 3000f,
                steps = 10f
            ),
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "margin" -> copy(margin = value as Float)
            "rotation" -> copy(rotation = value as Float)
            "enabled" -> copy(enabled = value as Boolean)
            "lod" -> copy(lod = value as Boolean)
            "minEdgeLength" -> copy(minEdgeLength = value as Float)
            "maxEdgeLength" -> copy(maxEdgeLength = value as Float)
            else -> this
        }
    }

    data class PartitionSimilarityRadialLayoutConfig(
        override val enabled: Boolean = true,
        val layerSpace: Float = 120.0f,
        val umapK: Int = 10,
        val umapIterations: Int = 250,
        val useTotalStateCount: Boolean = true,
        val useTotalEventCount: Boolean = true,
        val useTotalTraceCount: Boolean = true,
        val useDeepestDepth: Boolean = true,
        val useSplittingFactor: Boolean = true,
        val useHasRepetition: Boolean = true,
        val useCombinedCycleTime: Boolean = true,
        val useActivitySequenceEncoding: Boolean = true,
        val useLempelZivComplexity: Boolean = true,
        override val lod: Boolean = false
    ) : LayoutConfig("Partition-Similarity-based Radial Tree Layout") {
        override fun getParameters(): Map<String, ParameterInfo> {
            return mapOf(
                "umapK" to ParameterInfo.NumberParameterInfo("UMAP K", 2, 50, 50),
                "umapIterations" to ParameterInfo.NumberParameterInfo("UMAP Iterations", 50, 500, 50),
                "layerSpace" to ParameterInfo.NumberParameterInfo("LayerSpace", 1.0f, 500.0f, 5.0f),
                "enabled" to ParameterInfo.BooleanParameterInfo("Enabled"),
                "lod" to ParameterInfo.BooleanParameterInfo("Level of Detail"),
                "useTotalStateCount" to ParameterInfo.BooleanParameterInfo("useTotalStateCount"),
                "useTotalEventCount" to ParameterInfo.BooleanParameterInfo("useTotalEventCount"),
                "useTotalTraceCount" to ParameterInfo.BooleanParameterInfo("useTotalTraceCount"),
                "useDeepestDepth" to ParameterInfo.BooleanParameterInfo("useDeepestDepth"),
                "useSplittingFactor" to ParameterInfo.BooleanParameterInfo("useSplittingFactor"),
                "useHasRepetition" to ParameterInfo.BooleanParameterInfo("useHasRepetition"),
                "useCombinedCycleTime" to ParameterInfo.BooleanParameterInfo("useCombinedCycleTime"),
                "useActivitySequenceEncoding" to ParameterInfo.BooleanParameterInfo("useActivitySequenceEncoding"),
                "useLempelZivComplexity" to ParameterInfo.BooleanParameterInfo("useLempelZivComplexity"),
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
                "lod" -> copy(lod = value as Boolean)
                "useTotalStateCount" -> copy(useTotalStateCount = value as Boolean)
                "useTotalEventCount" -> copy(useTotalEventCount = value as Boolean)
                "useDeepestDepth" -> copy(useDeepestDepth = value as Boolean)
                "useSplittingFactor" -> copy(useSplittingFactor = value as Boolean)
                "useHasRepetition" -> copy(useHasRepetition = value as Boolean)
                "useCombinedCycleTime" -> copy(useCombinedCycleTime = value as Boolean)
                "useActivitySequenceEncoding" -> copy(useActivitySequenceEncoding = value as Boolean)
                "useLempelZivComplexity" -> copy(useLempelZivComplexity = value as Boolean)
                else -> this
            }
        }
    }

    data class DirectAngularConfig(
        val layerSpace: Float = 50.0f,
        val rotation: Float = 0.0f,
        override val enabled: Boolean = true,
        override val lod: Boolean = false
    ) : LayoutConfig("Direct Angular Tree Layout") {
        override fun getParameters() = mapOf(
            "layerSpace" to ParameterInfo.NumberParameterInfo("Layer Space", 10.0f, 200.0f, 5.0f),
            "rotation" to ParameterInfo.NumberParameterInfo("Rotation", 0.0f, 360.0f, 1.0f),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled"),
            "lod" to ParameterInfo.BooleanParameterInfo("Level of Detail")
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "layerSpace" -> copy(layerSpace = value as Float)
            "rotation" -> copy(rotation = value as Float)
            "enabled" -> copy(enabled = value as Boolean)
            "lod" -> copy(lod = value as Boolean)
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
        val umapK: Int = 10,
        val iterations: Int = 300,

        // Layout parameters
        val canvasWidth: Float = 2000.0f,
        val canvasHeight: Float = 2000.0f,

        override val enabled: Boolean = true,
    ) : LayoutConfig("State Clustering Layout") {

        override val lod: Boolean
            get() = false

        override fun getParameters() = mapOf(
            // Graph embedding
            "useGraphEmbedding" to ParameterInfo.BooleanParameterInfo("Use Graph Embedding"),
            "graphEmbeddingDims" to ParameterInfo.NumberParameterInfo("Graph Embedding Dims", 2, 128, 2),
            "walkLength" to ParameterInfo.NumberParameterInfo("Walk Length", 5, 50, 5),
            "walksPerVertex" to ParameterInfo.NumberParameterInfo("Walks Per Vertex", 5, 50, 5),
            "windowSize" to ParameterInfo.NumberParameterInfo("Window Size", 3, 20, 1),
            // Feature embedding
            "useFeatureEmbedding" to ParameterInfo.BooleanParameterInfo("Use Feature Embedding"),
            "featureEmbeddingDims" to ParameterInfo.NumberParameterInfo("Feature Embedding Dims", 2, 100, 100),
            // Feature flags
            "useDepthFeature" to ParameterInfo.BooleanParameterInfo("Use Depth Feature"),
            "useOutgoingTransitions" to ParameterInfo.BooleanParameterInfo("Use Outgoing Transitions"),
            "usePartitionValue" to ParameterInfo.BooleanParameterInfo("Use Partition Value"),
            "useSequenceLength" to ParameterInfo.BooleanParameterInfo("Use Sequence Length"),
            "useCycleTime" to ParameterInfo.BooleanParameterInfo("Use Cycle Time"),
            "usePathLength" to ParameterInfo.BooleanParameterInfo("Use Path Length"),
            "useActivity" to ParameterInfo.BooleanParameterInfo("Use Activity"),
            // Reduction parameters
            "umapK" to ParameterInfo.NumberParameterInfo("UMAP K", 2, 50, 50),
            "Iterations" to ParameterInfo.NumberParameterInfo("Iterations", 0, 100, 100),
            // Layout parameters
            "canvasWidth" to ParameterInfo.NumberParameterInfo("Canvas Width", 500.0f, 5000.0f, 100.0f),
            "canvasHeight" to ParameterInfo.NumberParameterInfo("Canvas Height", 500.0f, 5000.0f, 100.0f),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled"),
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
            "umapK" -> copy(umapK = value as Int)
            "Iterations" -> copy(iterations = value as Int)
            "canvasWidth" -> copy(canvasWidth = value as Float)
            "canvasHeight" -> copy(canvasHeight = value as Float)
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
        val useTotalStateCount: Boolean = true,
        val useTotalEventCount: Boolean = true,
        val useTotalTraceCount: Boolean = true,
        val useDeepestDepth: Boolean = true,
        val useSplittingFactor: Boolean = true,
        val useHasRepetition: Boolean = true,
        val useCombinedCycleTime: Boolean = true,
        val useActivitySequenceEncoding: Boolean = true,
        val useLempelZivComplexity: Boolean = true,
    ) : LayoutConfig("Partition Clustering Layout") {

        override val lod: Boolean
            get() = false

        override fun getParameters() = mapOf(
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled"),
            "umapK" to ParameterInfo.NumberParameterInfo("UMAP K", 2, 50, 50),
            "umapIterations" to ParameterInfo.NumberParameterInfo("UMAP Iterations", 50, 500, 50),
            "canvasWidth" to ParameterInfo.NumberParameterInfo("Canvas Width", 500.0f, 5000.0f, 100.0f),
            "canvasHeight" to ParameterInfo.NumberParameterInfo("Canvas Height", 500.0f, 5000.0f, 100.0f),
            "useTotalStateCount" to ParameterInfo.BooleanParameterInfo("useTotalStateCount"),
            "useTotalEventCount" to ParameterInfo.BooleanParameterInfo("useTotalEventCount"),
            "useTotalTraceCount" to ParameterInfo.BooleanParameterInfo("useTotalTraceCount"),
            "useDeepestDepth" to ParameterInfo.BooleanParameterInfo("useDeepestDepth"),
            "useSplittingFactor" to ParameterInfo.BooleanParameterInfo("useSplittingFactor"),
            "useHasRepetition" to ParameterInfo.BooleanParameterInfo("useHasRepetition"),
            "useCombinedCycleTime" to ParameterInfo.BooleanParameterInfo("useCombinedCycleTime"),
            "useActivitySequenceEncoding" to ParameterInfo.BooleanParameterInfo("useActivitySequenceEncoding"),
            "useLempelZivComplexity" to ParameterInfo.BooleanParameterInfo("useLempelZivComplexity"),
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "enabled" -> copy(enabled = value as Boolean)
            "umapK" -> copy(umapK = value as Int)
            "umapIterations" -> copy(umapIterations = value as Int)
            "canvasWidth" -> copy(canvasWidth = value as Float)
            "canvasHeight" -> copy(canvasHeight = value as Float)
            "useTotalStateCount" -> copy(useTotalStateCount = value as Boolean)
            "useTotalEventCount" -> copy(useTotalEventCount = value as Boolean)
            "useDeepestDepth" -> copy(useDeepestDepth = value as Boolean)
            "useSplittingFactor" -> copy(useSplittingFactor = value as Boolean)
            "useHasRepetition" -> copy(useHasRepetition = value as Boolean)
            "useCombinedCycleTime" -> copy(useCombinedCycleTime = value as Boolean)
            "useActivitySequenceEncoding" -> copy(useActivitySequenceEncoding = value as Boolean)
            "useLempelZivComplexity" -> copy(useLempelZivComplexity = value as Boolean)
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
        val seed: Int = 42,
        val minEdgeLength: Float = 10.0f,
        val maxEdgeLength: Float = 1000.0f,
        val LABEL_OVERLAP_FORCE_STRENGTH: Float = 1.0f,
        val EDGE_LENGTH_FORCE_STRENGTH: Float = 1.0f,
        val DISTRIBUTION_FORCE_STRENGTH: Float = 0.1f,
        override val lod: Boolean = false
    ) : LayoutConfig("(Parallel) Readable Tree Layout") {
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
                    max = 200,
                    steps = 200
                ),
                "minEdgeLength" to ParameterInfo.NumberParameterInfo(
                    name = "minEdgeLength",
                    min = 0f,
                    max = 1000f,
                    steps = 10f
                ),
                "maxEdgeLength" to ParameterInfo.NumberParameterInfo(
                    name = "maxEdgeLength",
                    min = 100f,
                    max = 3000f,
                    steps = 10f
                ),
                "LABEL_OVERLAP_FORCE_STRENGTH" to ParameterInfo.NumberParameterInfo(
                    name = "LABEL_OVERLAP_FORCE_STRENGTH",
                    min = 0.0f,
                    max = 1.0f,
                    steps = 0.1f
                ),
                "EDGE_LENGTH_FORCE_STRENGTH" to ParameterInfo.NumberParameterInfo(
                    name = "EDGE_LENGTH_FORCE_STRENGTH",
                    min = 0.0f,
                    max = 1.0f,
                    steps = 0.1f
                ),
                "DISTRIBUTION_FORCE_STRENGTH" to ParameterInfo.NumberParameterInfo(
                    name = "DISTRIBUTION_FORCE_STRENGTH",
                    min = 0.0f,
                    max = 1.0f,
                    steps = 0.1f
                ),
                "lod" to ParameterInfo.BooleanParameterInfo("Level of Detail")
            )
        }

        override fun updateParameter(
            name: String,
            value: Any
        ) = when (name) {
            "enabled" -> copy(enabled = value as Boolean)
            "initialization" -> copy(initializer = value as PRTInitialLayout)
            "iterations" -> copy(iterations = value as Int)
            "minEdgeLength" -> copy(minEdgeLength = value as Float)
            "maxEdgeLength" -> copy(maxEdgeLength = value as Float)
            "LABEL_OVERLAP_FORCE_STRENGTH" -> copy(LABEL_OVERLAP_FORCE_STRENGTH = value as Float)
            "EDGE_LENGTH_FORCE_STRENGTH" -> copy(EDGE_LENGTH_FORCE_STRENGTH = value as Float)
            "DISTRIBUTION_FORCE_STRENGTH" -> copy(DISTRIBUTION_FORCE_STRENGTH = value as Float)
            "lod" -> copy(lod = value as Boolean)
            else -> this
        }
    }
}