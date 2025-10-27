package moritz.lindner.masterarbeit.epa.features.partitioncombination

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State

class StatePartitionsCollection<T : Comparable<T>>(
    private val extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>
) : MutableMap<Int, MutableSet<State>> by mutableMapOf() {

    private val epaService = EpaService<T>()

    fun addStateAndUpdateAllOtherPartitions(state: State) {
        val partition = extendedPrefixAutomaton.partition(state)
        addPartitionToState(state, partition)

        epaService.getPathFromRoot(state).forEach { stateOnPath ->
            addPartitionToState(stateOnPath, partition)
        }
    }

    private fun addPartitionToState(state: State, partition: Int) {
        getOrPut(partition) { mutableSetOf() }.add(state)
    }

    fun getStates(c: Int): Set<State> =
        this[c] ?: emptySet()

    fun getAllPartitions(): Set<Int> = keys

    fun hasRepetition(c: Int): Boolean {
        val allActivities = getStates(c)
            .mapNotNull { state ->
                (state as? State.PrefixState)?.via
            }

        return allActivities.distinct().count() < allActivities.count()
    }

    fun splittingFactor(c: Int): Int {
        return getStates(c).sumOf { state ->
            epaService.outgoingTransitions(extendedPrefixAutomaton, state).size
        }
    }

    override fun toString(): String {
        return buildString {
            forEach { partition, states ->
                val partitions = states.joinToString(",")
                appendLine("$partition: $partitions")
            }
        }
    }
}