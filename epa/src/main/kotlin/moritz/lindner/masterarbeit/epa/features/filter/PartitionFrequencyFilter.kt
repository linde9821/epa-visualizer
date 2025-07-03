package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequencyVisitor

/**
 * Filters an [ExtendedPrefixAutomaton] by removing all states and transitions
 * that belong to partitions with a normalized frequency below a given [threshold].
 *
 * Partitions are evaluated using a [NormalizedPartitionFrequencyVisitor],
 * and only those with frequency >= [threshold] (or partition 0, which is always retained)
 * are kept.
 *
 * @param T The timestamp type used in the automaton's events.
 * @property threshold The minimum normalized frequency a partition must have to be included.
 */
class PartitionFrequencyFilter<T : Comparable<T>>(
    private val threshold: Float,
) : EpaFilter<T> {
    /**
     * Applies the frequency-based partition filtering logic to the given automaton.
     *
     * @param epa The automaton to filter.
     * @return A new [ExtendedPrefixAutomaton] with only frequent partitions retained.
     */
    override fun apply(epa: ExtendedPrefixAutomaton<T>): ExtendedPrefixAutomaton<T> {
        val normalizedPartitionFrequencyVisitor = NormalizedPartitionFrequencyVisitor<T>()

        epa.copy().acceptDepthFirst(normalizedPartitionFrequencyVisitor)

        val partitions =
            epa
                .getAllPartitions()
                .associateWith { partition ->
                    normalizedPartitionFrequencyVisitor.frequencyByPartition(partition)
                }.filter { (a, b) -> b >= threshold || a == 0 }
                .keys
                .toList()

        // remove orphans
        val filteredStates =
            epa.states
                .filter { state ->
                    val partition = epa.partition(state)
                    partition in partitions
                }.toSet()

        val filteredActivities =
            epa.activities
                .filter { activity ->
                    filteredStates.any { state ->
                        if (state is State.PrefixState) {
                            activity == state.via
                        } else {
                            false
                        }
                    }
                }.toSet()

        val filteredTransitions =
            epa.transitions
                .filter { transition ->
                    transition.activity in filteredActivities &&
                        transition.start in filteredStates &&
                        transition.end in filteredStates
                }.toSet()

        val partitionByState = filteredStates.associateWith { state -> epa.partition(state) }
        val sequenceByState = filteredStates.associateWith { state -> epa.sequence(state) }

        return ExtendedPrefixAutomaton(
            eventLogName = epa.eventLogName,
            states = filteredStates,
            activities = filteredActivities,
            transitions = filteredTransitions,
            partitionByState = partitionByState,
            sequenceByState = sequenceByState,
        )
    }
}
