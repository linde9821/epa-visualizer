package moritz.lindner.masterarbeit.epa.features.lod

data class AggregationInfo(
    val hiddenChildCount: Int,
    val hiddenPartitions: Set<Int>,
    val totalEventCount: Int
)