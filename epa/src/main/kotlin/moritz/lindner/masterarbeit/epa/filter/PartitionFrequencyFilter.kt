package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.statistics.NormalizedPartitionFrequencyVisitor

class PartitionFrequencyFilter<T : Comparable<T>>(
    private val threshold: Float,
) : EpaFilter<T> {
    private val normalizedPartitionFrequencyVisitor = NormalizedPartitionFrequencyVisitor<T>()

    override fun apply(epa: ExtendedPrefixAutomata<T>): ExtendedPrefixAutomata<T> {
        epa.acceptDepthFirst(normalizedPartitionFrequencyVisitor)

        val partitions =
            epa
                .getAllPartitions()
                .associateWith { partition ->
                    normalizedPartitionFrequencyVisitor.frequencyByPartition(partition)
                }.filter { (a, b) -> b > threshold || a == 0 }
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
                            true
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

        return ExtendedPrefixAutomata(
            states = filteredStates,
            activities = filteredActivities,
            transitions = filteredTransitions,
            partitionByState = partitionByState,
            sequenceByState = sequenceByState,
        )
    }
}
