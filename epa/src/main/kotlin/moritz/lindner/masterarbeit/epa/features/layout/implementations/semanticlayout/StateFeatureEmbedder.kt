package moritz.lindner.masterarbeit.epa.features.layout.implementations.semanticlayout

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class StateFeatureEmbedder(
    private val epa: ExtendedPrefixAutomaton<Long>,
    private val config: SemanticLayoutConfig,
    private val progressCallback: EpaProgressCallback?
) {
    private val epaService = EpaService<Long>()

    class TransitionCounter<T : Comparable<T>>(
        private val progressCallback: EpaProgressCallback? = null
    ) : AutomatonVisitor<T> {

        val incommingByState = mutableMapOf<State, Int>()
        val outcommingByState = mutableMapOf<State, Int>()

        override fun visit(
            extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
            transition: Transition,
            depth: Int
        ) {
            outcommingByState[transition.start] = outcommingByState.getOrDefault(transition.start, 0) + 1
            incommingByState[transition.start] = incommingByState.getOrDefault(transition.end, 0) + 1
        }

        override fun onProgress(current: Long, total: Long) {
            progressCallback?.onProgress(current.toInt(), total.toInt(), "transition counting")
        }
    }

    fun computeEmbeddings(): Map<State, DoubleArray> {
        val allActivities = epa.activities.toList()
        val cycleTimeByState = epaService.computeAllCycleTimes(
            extendedPrefixAutomaton = epa,
            minus = Long::minus,
            average = { cycleTimes ->
                if (cycleTimes.isEmpty()) {
                    0f
                } else cycleTimes.average().toFloat()
            },
            progressCallback = progressCallback
        )

        val counter = TransitionCounter<Long>(progressCallback)
        epa.acceptDepthFirst(counter)

        var c = 0
        val total = epa.states.size

        return epa.states.associateWith { state ->
            progressCallback?.onProgress(c++, total, "feature embedding")
            val features = mutableListOf<Double>()

            // Structural features
            features.add(epaService.getDepth(state).toDouble())
            features.add(counter.incommingByState[state]?.toDouble() ?: 0.0)
            features.add(counter.outcommingByState[state]?.toDouble() ?: 0.0)

            // EPA-specific features
            features.add(epa.partition(state).toDouble())
            features.add(epa.sequence(state).size.toDouble())
            features.add(cycleTimeByState[state]!!.toDouble())

            // Path features
            val path = epaService.getPathFromRoot(state)
            features.add(path.size.toDouble())

            // Activity encoding (simplified one-hot)
            val lastActivity = when (state) {
                is State.PrefixState -> state.via
                State.Root -> null
            }

            // might be larger than embedding size
            allActivities.forEach { activity ->
                features.add(if (activity == lastActivity) 1.0 else 0.0)
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