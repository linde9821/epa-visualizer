package moritz.lindner.masterarbeit.epa.features.partitioncombination

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State

class StatePartitionsCollection<T : Comparable<T>>: MutableMap<Int, MutableSet<State>> by mutableMapOf() {

    private val epaService = EpaService<T>()

    fun addStateAndUpdateAllOtherPartitions(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>, state: State) {
        val partition = extendedPrefixAutomaton.partition(state)
        addPartitionToState(state, partition)

        epaService.getPathFromRoot(state).forEach { stateOnPath ->
            addPartitionToState(stateOnPath, partition)
        }
    }

    private fun addPartitionToState(state: State, partition: Int) {
        getOrPut(partition) { mutableSetOf() }.add(state)
    }

    fun getPartitions(c: Int): Set<State> =
        this[c] ?: emptySet()

    fun getAllStates(): Set<Int> = keys

    override fun toString(): String {
        return buildString {
            forEach { state, partitions ->
                val partitions = partitions.joinToString(",")
                appendLine("$state: $partitions")
            }
        }
    }
}