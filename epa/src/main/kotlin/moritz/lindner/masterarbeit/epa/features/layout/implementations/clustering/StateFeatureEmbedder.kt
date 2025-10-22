package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor


class PartitionFoo: AutomatonVisitor<Long> {

    private val statesByPartition = HashMap<Int, MutableList<State>>()

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>) {
        super.onEnd(extendedPrefixAutomaton)
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        state: State,
        depth: Int
    ) {
        val c = extendedPrefixAutomaton.partition(state)

        statesByPartition.compute(c) { _, v ->
            if (v == null) mutableListOf(state) else {
                v.add(state)
                v
            }
        }
    }
}

//partition length (basically seq.length)
//depth of finale state in partition
//total state count (including parent partitions)
//total event count (including parent partitions)
//total trace count (including parent partitions)
//splitting factor (how many new different partitions are created along the path)
//total combined cycle time (including parent partitions)
//has repatitions (including parent partitions)
//unique activities (including parent partitions) one hot encoded
//prefix seq (n-gram encoded) (including parent partitions)
//activity Count/Occurrence Encoding (including parent partitions)
//cycle time variance
//Median cycle time
//depth of partition creation
//Activity diversity (Shannon entropy)
//Cycle time skewness: Is the distribution symmetric or does it have a long tail? Distinguishes "consistently fast with rare slow cases" from "consistently slow" Formula: E[(X - μ)³] / σ³
//Lempel-Ziv Complexity LZ76 Algorithm (Most Common for Complexity Measurement)
class PartitionFeatureEmbedder() {



    fun computeEmbedding(epa: ExtendedPrefixAutomaton<Long>) {
        val partitions = epa.states.map { state ->
            epa.partition(state)
        }.distinct()

        partitions.map { c ->
            val features = mutableListOf<Double>()
        }
    }


    // https://github.com/Naereen/Lempel-Ziv_Complexity
    fun lempelZivComplexity(sequence: List<String>): Int {
        if (sequence.isEmpty()) return 0

        val subStrings = mutableSetOf<String>()
        val delimiter = "|" // separator for joining
        var ind = 0
        var inc = 1

        while (ind + inc <= sequence.size) {
            val subStr = sequence.subList(ind, ind + inc).joinToString(delimiter)

            if (subStr in subStrings) {
                inc++
            } else {
                subStrings.add(subStr)
                ind += inc
                inc = 1
            }
        }

        return subStrings.size
    }
}

class StateFeatureEmbedder(
    private val epa: ExtendedPrefixAutomaton<Long>,
    private val config: LayoutConfig.ClusteringLayoutConfig,
    private val progressCallback: EpaProgressCallback?
) {
    private val epaService = EpaService<Long>()

    class OutgoingTransitionCounter<T : Comparable<T>>(
        private val progressCallback: EpaProgressCallback? = null
    ) : AutomatonVisitor<T> {

        val outcommingByState = mutableMapOf<State, Int>()

        override fun visit(
            extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
            transition: Transition,
            depth: Int
        ) {
            outcommingByState[transition.start] = outcommingByState.getOrDefault(transition.start, 0) + 1
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

        val counter = OutgoingTransitionCounter<Long>(progressCallback)
        epa.acceptDepthFirst(counter)

        var c = 0
        val total = epa.states.size

        return epa.states.associateWith { state ->
            progressCallback?.onProgress(c++, total, "feature embedding")
            val features = mutableListOf<Double>()

            with(config) {
                // Structural features
                if (useDepthFeature) features.add(epaService.getDepth(state).toDouble())
                if (useOutgoingTransitions) features.add(counter.outcommingByState[state]?.toDouble() ?: 0.0)
                // EPA-specific features
                if (usePartitionValue) features.add(epa.partition(state).toDouble())
                if (useSequenceLength) features.add(epa.sequence(state).size.toDouble())
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