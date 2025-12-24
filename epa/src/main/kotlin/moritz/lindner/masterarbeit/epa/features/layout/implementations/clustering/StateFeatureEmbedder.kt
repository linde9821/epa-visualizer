package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig

class StateFeatureEmbedder(
    private val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val config: LayoutConfig.StateClusteringLayoutConfig,
    private val progressCallback: EpaProgressCallback?
) {
    private val epaService = EpaService<Long>()

    fun computeEmbeddings(): Map<State, DoubleArray> {
        val allActivities = extendedPrefixAutomaton.activities.toList()
        val cycleTimeByState = epaService.computeAverageStateCycleTimesOfAllStates(
            extendedPrefixAutomaton = extendedPrefixAutomaton,
            minus = Long::minus,
            average = { cycleTimes ->
                if (cycleTimes.isEmpty()) {
                    0f
                } else cycleTimes.average().toFloat()
            },
            progressCallback = progressCallback
        )

        var c = 0
        val total = extendedPrefixAutomaton.states.size

        return extendedPrefixAutomaton.states.associateWith { state ->
            progressCallback?.onProgress(c++, total, "feature embedding")
            val features = mutableListOf<Double>()

            with(config) {
                // Structural features
                if (useDepthFeature) features.add(epaService.getDepth(state).toDouble())
                if (useOutgoingTransitions) features.add(
                    epaService.outgoingTransitions(
                        extendedPrefixAutomaton,
                        state
                    ).size.toDouble()
                )
                // EPA-specific features
                if (usePartitionValue) features.add(extendedPrefixAutomaton.partition(state).toDouble())
                if (useSequenceLength) features.add(extendedPrefixAutomaton.sequence(state).size.toDouble())
                if (useCycleTime) features.add(cycleTimeByState[state]!!.toDouble())
                // Path features
                if (usePathLength) {
                    val path = epaService.getPathFromRoot(state)
                    features.add(path.size.toDouble())
                }
                if (useActivity) {
                    // Activity encoding (simplified one-hot)
                    val statesActivity = when (state) {
                        is State.PrefixState -> state.via
                        State.Root -> null
                    }
                    // might be larger than embedding size
                    allActivities.forEach { activity ->
                        features.add(if (activity == statesActivity) 1.0 else 0.0)
                    }
                }
            }

            // Pad or truncate to desired size
            val result = DoubleArray(config.featureEmbeddingDims)
            features.take(config.featureEmbeddingDims).forEachIndexed { i, value ->
                result[i] = value
            }

            result
        }
    }
}