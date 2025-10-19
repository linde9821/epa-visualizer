package moritz.lindner.masterarbeit.epa.features.layout.implementations.semanticlayout

data class SemanticLayoutConfig(
    // Embedding parameters
    val graphEmbeddingDims: Int = 16,
    val featureEmbeddingDims: Int = 16,

    // DeepWalk parameters
    val walkLength: Int = 10,
    val walksPerVertex: Int = 10,
    val windowSize: Int = 5,

    // Reduction parameters
    val reductionMethod: ReductionMethod = ReductionMethod.UMAP,
    val targetDimensions: Int = 2,

    // Clustering parameters
    val enableClustering: Boolean = true,
    val dbscanMinPts: Int = 5,
    val dbscanEpsilon: Double = 0.5,

    // Layout parameters
    val canvasWidth: Float = 2000.0f,
    val canvasHeight: Float = 2000.0f,
    val nodeRadius: Float = 5.0f,
    val padding: Float = 50.0f,
    val clusterPadding: Float = 20.0f,

    // Force-directed parameters
    val enableForceDirected: Boolean = true,
    val repulsionStrength: Float = 100.0f,
    val iterations: Int = 10
)