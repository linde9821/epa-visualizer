package moritz.lindner.masterarbeit.epa.visitor.case

import moritz.lindner.masterarbeit.epa.domain.State
import java.util.TreeMap

/**
 * Represents the animation or progression of a case over time, where each timestamp maps to a specific state.
 *
 * @param T The type used for timestamps, which must be comparable (e.g., Int, Long, LocalDateTime).
 * @property caseIdentifier A unique identifier for the case.
 * @property stateAtTimestamp A sorted map (by timestamp) of states representing the case's history over time.
 * @property totalAmountOfEvents The total number of state changes (events) recorded for this case.
 */
data class CaseAnimation<T : Comparable<T>>(
    private val caseIdentifier: String,
    private val stateAtTimestamp: TreeMap<T, State>,
    val totalAmountOfEvents: Int,
) {
    private val sortedEntries = stateAtTimestamp.entries.toList()

    /**
     * Retrieves all [State]s that occurred strictly before the given [timestamp].
     *
     * @param timestamp The cutoff timestamp (exclusive).
     * @return A list of states ordered by their timestamp, occurring before [timestamp].
     */
    fun getStateUpTillTimestamp(timestamp: T): List<State> = stateAtTimestamp.headMap(timestamp, false).values.toList()

    /**
     * Retrieves all [State]s that occurred strictly after the given [timestamp].
     *
     * @param timestamp The starting timestamp (exclusive).
     * @return A list of states ordered by their timestamp, occurring after [timestamp].
     */
    fun getStateFromTimestamp(timestamp: T): List<State> = stateAtTimestamp.tailMap(timestamp, false).values.toList()

    /**
     * Retrieves the N-th state and its timestamp based on chronological order of timestamps.
     *
     * @param n The zero-based index of the desired entry.
     * @return A pair of timestamp and state at position [n], or `null` if [n] is out of bounds.
     */
    fun getNthEntry(n: Int): Pair<T, State>? {
        val entry = sortedEntries.getOrNull(n)
        return entry?.let { (key, value) -> key to value }
    }

    override fun toString(): String =
        "CaseAnimation(caseIdentifier='$caseIdentifier', stateAtTimestamp=$stateAtTimestamp, totalAmountOfEvents=$totalAmountOfEvents)"

    fun getFirst(): Pair<T, State> {
        val entry = sortedEntries.first()
        return entry.let { (key, value) -> key to value }
    }
}
