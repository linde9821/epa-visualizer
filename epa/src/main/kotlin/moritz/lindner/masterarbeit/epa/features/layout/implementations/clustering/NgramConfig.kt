package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

data class NgramConfig(
    val n: Int = 3,
    val includeUnigrams: Boolean = true,
    val includeBigrams: Boolean = true,
    val maxNgramFeatures: Int = 100
)