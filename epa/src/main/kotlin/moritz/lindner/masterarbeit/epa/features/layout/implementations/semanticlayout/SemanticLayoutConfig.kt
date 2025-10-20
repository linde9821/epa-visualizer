package moritz.lindner.masterarbeit.epa.features.layout.implementations.semanticlayout

data class SemanticLayoutConfig(

    val useGraphEmbedding: Boolean = false,
    val graphEmbeddingDims: Int = 16,
    val walkLength: Int = 10,
    val walksPerVertex: Int = 10,
    val windowSize: Int = 5,

    val useFeatureEmedding: Boolean = true,
    val featureEmbeddingDims: Int = 16,

    val useDepthFeature: Boolean = true,
    val useOutgoingTransitions: Boolean = true,
    val usePartitionValue: Boolean = true,
    val useSequenceLength: Boolean = true,
    val useCycleTime: Boolean = true,
    val usePathLength: Boolean = true,
    val useActivity: Boolean = true,

    // Reduction parameters
    val reductionMethod: ReductionMethod = ReductionMethod.UMAP,

    // Layout parameters
    val canvasWidth: Float = 2000.0f,
    val canvasHeight: Float = 2000.0f,
    val nodeRadius: Float = 5.0f,
    val padding: Float = 50.0f,

    // Force-directed parameters
    val enableForceDirected: Boolean = true,
    val repulsionStrength: Float = 100.0f,
    val forceDirectedLayoutIterations: Int = 10
)