package moritz.lindner.masterarbeit.epa.features.animation

import moritz.lindner.masterarbeit.epa.domain.State

/**
 * Represents a timeline-based animation of an event log, where each
 * [State] is modeled as a [TimedState] and associated with an interval
 * `[from, to)` during which it is considered active.
 *
 * This class supports efficient temporal queries to determine which states
 * are active at any point in time, enabling time-based visualizations
 * or simulations over an entire log (e.g., for animation or replay).
 *
 * @param T The timestamp type (e.g., [Long], [Int], or
 *    [java.time.LocalDateTime]), which must be [Comparable].
 * @property identifier A unique label identifying the origin of this
 *    animation (e.g., log name or case ID).
 * @property timedStates A list of all recorded [TimedState] intervals
 *    across the event log.
 * @property totalAmountOfEvents The total number of [TimedState] entries
 *    in the animation.
 */
data class EventLogAnimation<T : Comparable<T>>(
    private val identifier: String,
    private val timedStates: List<TimedState<T>>,
    val totalAmountOfEvents: Int,
) {
    private val firstTimedState =
        timedStates.minByOrNull { it.startTime } ?: throw IllegalStateException("No states in animation")
    private val lastTimedState =
        timedStates.maxByOrNull { it.endTime } ?: throw IllegalStateException("No states in animation")

    private val segmentTree = TimedStateSegmentTree<T>(timedStates)

    /**
     * Returns all [TimedState]s that are active at a specific [timestamp].
     *
     * A state is considered active if the [timestamp] falls within the
     * interval `[from, to)`. If [to] is `null`, the interval is considered
     * open-ended.
     *
     * @param timestamp The point in time to query.
     * @return A list of all [TimedState]s active at the given time.
     */
    fun getActiveStatesAt(timestamp: T): List<TimedState<T>> {
        return segmentTree.getActiveStatesAt(timestamp)
    }

    /**
     * Returns the first recorded [TimedState] in the animation timeline.
     *
     * @return A pair of `(from timestamp, TimedState)` for the earliest entry.
     * @throws IllegalStateException if no states are recorded.
     */
    fun getFirst(): Pair<T, TimedState<T>> {
        return firstTimedState.startTime to firstTimedState
    }

    /**
     * Returns the last [TimedState] in the animation, based on the latest `to`
     * value (or `from` if `to` is null).
     *
     * This represents the logical end of the animation.
     *
     * @return A pair of `(timestamp, TimedState)` representing the latest
     *    visible state.
     * @throws IllegalStateException if the animation contains no states.
     */
    fun getLast(): Pair<T, TimedState<T>> {
        return lastTimedState.startTime to lastTimedState
    }

    /**
     * Retrieves the N-th [TimedState] from the original input order.
     *
     * Useful for slider-based UI playback or timeline scrubbing.
     *
     * @param n Zero-based index of the entry.
     * @return A pair of `(from timestamp, TimedState)`, or `null` if the index
     *    is out of bounds.
     */
    fun getNthEntry(n: Int): Pair<T, TimedState<T>>? = timedStates.getOrNull(n)?.let { it.startTime to it }
}

