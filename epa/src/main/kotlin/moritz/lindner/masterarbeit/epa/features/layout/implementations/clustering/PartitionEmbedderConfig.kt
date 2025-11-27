package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig

data class PartitionEmbedderConfig(
    val useTotalStateCount: Boolean = true,
    val useTotalEventCount: Boolean = true,
    val useTotalTraceCount: Boolean = true,
    val useDeepestDepth: Boolean = true,
    val useSplittingFactor: Boolean = true,
    val useHasRepetition: Boolean = true,
    val useCombinedCycleTime: Boolean = true,
    val useActivitySequenceEncoding: Boolean = true,
    val useLempelZivComplexity: Boolean = true,
) {
    companion object {
        fun from(c: LayoutConfig.PartitionClusteringLayoutConfig): PartitionEmbedderConfig {
            return PartitionEmbedderConfig(
                useTotalStateCount = c.useTotalStateCount,
                useTotalEventCount = c.useTotalEventCount,
                useTotalTraceCount = c.useTotalTraceCount,
                useDeepestDepth = c.useDeepestDepth,
                useSplittingFactor = c.useSplittingFactor,
                useHasRepetition = c.useHasRepetition,
                useCombinedCycleTime = c.useCombinedCycleTime,
                useActivitySequenceEncoding = c.useActivitySequenceEncoding,
                useLempelZivComplexity = c.useLempelZivComplexity,
            )
        }

        fun from(c: LayoutConfig.PartitionSimilarityRadialLayoutConfig): PartitionEmbedderConfig {
            return PartitionEmbedderConfig(
                useTotalStateCount = c.useTotalStateCount,
                useTotalEventCount = c.useTotalEventCount,
                useTotalTraceCount = c.useTotalTraceCount,
                useDeepestDepth = c.useDeepestDepth,
                useSplittingFactor = c.useSplittingFactor,
                useHasRepetition = c.useHasRepetition,
                useCombinedCycleTime = c.useCombinedCycleTime,
                useActivitySequenceEncoding = c.useActivitySequenceEncoding,
                useLempelZivComplexity = c.useLempelZivComplexity,
            )
        }
    }
}