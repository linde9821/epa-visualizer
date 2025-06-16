package moritz.lindner.masterarbeit.epa.visitor.animation

import moritz.lindner.masterarbeit.epa.domain.State
import java.util.TreeMap

/**
 * Represents the animation or progression of a case over time, where each timestamp maps to a list of states.
 *
 * @param T The type used for timestamps, which must be comparable (e.g., Int, Long, LocalDateTime).
 * @property caseIdentifier A unique identifier for the case.
 * @property stateAtTimestamp A sorted map (by timestamp) of states representing the case's history over time.
 * @property totalAmountOfEvents The total number of state changes (events) recorded for this case.
 */
data class EventLogAnimation<T : Comparable<T>>(
    private val caseIdentifier: String,
    private val stateAtTimestamp: TreeMap<T, List<State>>,
    val totalAmountOfEvents: Int,
) {
    private val sortedEntries = stateAtTimestamp.entries.toList()

    /**
     * Retrieves all [State]s that occurred strictly before the given [timestamp].
     *
     * @param timestamp The cutoff timestamp (exclusive).
     * @return A list of states ordered by their timestamp, occurring before [timestamp].
     */
    fun getStateUpTillTimestamp(timestamp: T): List<State> = stateAtTimestamp.headMap(timestamp, false).values.flatten()

    /**
     * Retrieves all [State]s that occurred strictly after the given [timestamp].
     *
     * @param timestamp The starting timestamp (exclusive).
     * @return A list of states ordered by their timestamp, occurring after [timestamp].
     */
    fun getStateFromTimestamp(timestamp: T): List<State> = stateAtTimestamp.tailMap(timestamp, false).values.flatten()

    /**
     * Retrieves the N-th state and its timestamp based on chronological order of timestamps.
     *
     * @param n The zero-based index of the desired state across all timestamps.
     * @return A pair of timestamp and state at position [n], or `null` if [n] is out of bounds.
     */
    fun getNthEntry(n: Int): Pair<T, State>? {
        var counter = 0
        for ((timestamp, states) in sortedEntries) {
            for (state in states) {
                if (counter == n) return timestamp to state
                counter++
            }
        }
        return null
    }

    /**
     * Retrieves the first [State] and its timestamp in chronological order.
     *
     * @return A pair of the first timestamp and its first [State].
     */
    fun getFirst(): Pair<T, State> {
        val (timestamp, states) = sortedEntries.first()
        return timestamp to states.first()
    }

    override fun toString(): String =
        "CaseAnimation(caseIdentifier='$caseIdentifier', stateAtTimestamp=$stateAtTimestamp, totalAmountOfEvents=$totalAmountOfEvents)"
}
