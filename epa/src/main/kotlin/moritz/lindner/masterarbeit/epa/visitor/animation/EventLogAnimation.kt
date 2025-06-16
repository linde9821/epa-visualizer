package moritz.lindner.masterarbeit.epa.visitor.animation

import moritz.lindner.masterarbeit.epa.domain.State
import java.util.TreeMap

data class EventLogAnimation<T : Comparable<T>>(
    private val caseIdentifier: String,
    private val timedStates: List<TimedState<T>>,
    val totalAmountOfEvents: Int,
) {
    private val sortedStates = timedStates.sortedBy { it.from }

    private val statesByInterval: TreeMap<T, MutableList<TimedState<T>>> =
        TreeMap<T, MutableList<TimedState<T>>>().apply {
            timedStates.forEach { timedState ->
                getOrPut(timedState.from) { mutableListOf() } += timedState
            }
        }

    /**
     * Efficiently retrieves all states active at the given timestamp.
     */
    fun getActiveStatesAt(timestamp: T): List<State> {
        val relevantEntries = statesByInterval.headMap(timestamp, true).values.flatten()
        return relevantEntries.filter { it.to == null || timestamp < it.to!! }.map { it.state }
    }

    fun getFirst(): Pair<T, State> {
        val first =
            sortedStates.firstOrNull()
                ?: throw IllegalStateException("No states in animation")
        return first.from to first.state
    }

    fun getLast(): Pair<T, State> {
        val last =
            sortedStates.maxByOrNull { it.to ?: it.from }
                ?: throw IllegalStateException("No states in animation")
        val endTimestamp = last.to ?: last.from
        return endTimestamp to last.state
    }

    fun getNthEntry(n: Int): Pair<T, State>? = timedStates.getOrNull(n)?.let { it.from to it.state }
}
