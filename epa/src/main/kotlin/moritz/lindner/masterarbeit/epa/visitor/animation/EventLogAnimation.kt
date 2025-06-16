package moritz.lindner.masterarbeit.epa.visitor.animation

import moritz.lindner.masterarbeit.epa.domain.State
import java.util.TreeMap

/**
 * Represents the animation or progression of an entire event log over time,
 * where each state is active during a defined time interval.
 *
 * Each state in the log is captured as a [TimedState], representing when that state becomes
 * active (`from`) and when it is no longer active (`to`). This allows querying which states
 * are "alive" at any given point in the timeline.
 *
 * @param T The timestamp type, which must be comparable (e.g., [Long], [Int], [LocalDateTime]).
 * @property identifier A label or identifier for the source of the animation (e.g., log name).
 * @property timedStates A list of state-time intervals for all cases in the log.
 * @property totalAmountOfEvents The total number of recorded [TimedState] entries across all cases.
 */
data class EventLogAnimation<T : Comparable<T>>(
    private val identifier: String,
    private val timedStates: List<TimedState<T>>,
    val totalAmountOfEvents: Int,
) {
    private val sortedStates = timedStates.sortedBy { it.from }

    /**
     * A map from each start timestamp to the list of [TimedState]s starting at that time.
     * Used to efficiently find all states that might be active at a given timestamp.
     */
    private val statesByInterval: TreeMap<T, MutableList<TimedState<T>>> =
        TreeMap<T, MutableList<TimedState<T>>>().apply {
            timedStates.forEach { timedState ->
                getOrPut(timedState.from) { mutableListOf() } += timedState
            }
        }

    /**
     * Retrieves all [State]s that are active at the given [timestamp].
     * A state is considered active if the timestamp is within the interval [from, to).
     *
     * @param timestamp The point in time to check for active states.
     * @return A list of active states at the given timestamp.
     */
    fun getActiveStatesAt(timestamp: T): List<TimedState<T>> {
        val relevantEntries = statesByInterval.headMap(timestamp, true).values.flatten()
        return relevantEntries.filter { it.to == null || timestamp < it.to!! }
    }

    /**
     * Retrieves the first state and its associated start timestamp in the animation.
     *
     * @return A [Pair] of (timestamp, state) for the first interval.
     * @throws IllegalStateException if the animation contains no states.
     */
    fun getFirst(): Pair<T, TimedState<T>> {
        val first =
            sortedStates.firstOrNull()
                ?: throw IllegalStateException("No states in animation")
        return first.from to first
    }

    /**
     * Retrieves the last state and its associated end timestamp in the animation.
     *
     * If a state has no defined `to` timestamp, its `from` timestamp is used instead.
     *
     * @return A [Pair] of (timestamp, TimedState) for the last interval.
     * @throws IllegalStateException if the animation contains no states.
     */
    fun getLast(): Pair<T, TimedState<T>> {
        val last =
            sortedStates.maxByOrNull { it.to ?: it.from }
                ?: throw IllegalStateException("No states in animation")
        val endTimestamp = last.to ?: last.from
        return endTimestamp to last
    }

    /**
     * Retrieves the N-th state in the sequence based on original order.
     *
     * @param n The zero-based index of the state interval.
     * @return A [Pair] of (from-timestamp, state) or `null` if index is out of bounds.
     */
    fun getNthEntry(n: Int): Pair<T, TimedState<T>>? = timedStates.getOrNull(n)?.let { it.from to it }
}
