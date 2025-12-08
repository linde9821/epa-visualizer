package moritz.lindner.masterarbeit.epa.features.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State

/**
 * Computes the normalized frequency of events per partition in an
 * [ExtendedPrefixAutomaton].
 *
 * The frequency is calculated as the number of events observed in each
 * partition divided by the total number of events across all partitions
 * and there parent partitions. The result is a value in [0.0, 1.0].
 *
 * This visitor must be run using
 * [ExtendedPrefixAutomaton.acceptDepthFirst] or
 * [acceptBreadthFirst] before querying the frequencies.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class NormalizedPartitionFrequencyVisitor<T : Comparable<T>>(
) {
    private val relativeFrequencyByPartition = HashMap<Int, Float>()
    private val epaService = EpaService<T>()

    fun build(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        normalizedStateFrequency: NormalizedStateFrequency
    ): NormalizedPartitionFrequency {
        val terminatingStatesByPartition = extendedPrefixAutomaton
            .states
            .groupBy(extendedPrefixAutomaton::partition)
            .mapValues { (partition, states) ->
                // Edge Case: Undefined partition of root
                if (partition == 0) {
                    State.Root
                } else {
                    val terminatingStateOfPartitionOrRoot = states.filter { state ->
                        epaService.isFinalState(extendedPrefixAutomaton, state)
                    }

                    require(terminatingStateOfPartitionOrRoot.size == 1) {
                        "There can only be one final state for each partition but $terminatingStateOfPartitionOrRoot are found for partition $partition"
                    }

                    terminatingStateOfPartitionOrRoot.first()
                }
            }

        terminatingStatesByPartition.forEach { partition, state ->
            val pathToRoot = epaService.getPathToRoot(state)
            val size = pathToRoot.size
            val combinedFreq = pathToRoot.map { stateOnPath ->
                normalizedStateFrequency.frequencyByState(stateOnPath)
            }.sum()

            val p = combinedFreq / size
            relativeFrequencyByPartition[partition] = p
        }


        return NormalizedPartitionFrequency(relativeFrequencyByPartition)
    }
}
