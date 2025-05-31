package moritz.lindner.masterarbeit.epa.visitor.case

import moritz.lindner.masterarbeit.epa.domain.State
import java.util.TreeMap

/**
 * Represents the animation or progression of a case over time, where each timestamp maps to a specific state.
 *
 * @param T The type used for timestamps, which must be comparable (e.g., Int, Long, LocalDateTime).
 * @property caseIdentifier A unique identifier for the case.
 * @property stateAtTimestamp A sorted map (by timestamp) of states representing the case's history over time.
 */
data class CaseAnimation<T : Comparable<T>>(
    val caseIdentifier: String,
    val stateAtTimestamp: TreeMap<T, State>,
    val totalAmountOfEvents: Int,
) {
    /**
     * Returns the [State] closest to the given [timestamp].
     *
     * If the exact timestamp is not found, it returns the state corresponding to the closest key — either the
     * largest timestamp less than or equal to [timestamp] (floor), or the smallest timestamp greater than or
     * equal to [timestamp] (ceiling), whichever is closer.
     *
     * @param timestamp The timestamp for which to retrieve the closest state.
     * @return The [State] at the closest timestamp, or null if the map is empty.
     */
    fun getStateAtTimestamp(timestamp: T): Pair<T, State?> {
        val floor = stateAtTimestamp.floorKey(timestamp)
        val ceiling = stateAtTimestamp.ceilingKey(timestamp)

        val closestKey =
            when {
                floor == null -> ceiling
                ceiling == null -> floor
                else -> if ((timestamp.compareTo(floor) < timestamp.compareTo(ceiling))) floor else ceiling
            }

        return closestKey to stateAtTimestamp[closestKey]
    }

    fun getStateUpTillTimestamp(timestamp: T): List<State> = stateAtTimestamp.headMap(timestamp, false).values.toList()

    fun getStateFromTimestamp(timestamp: T): List<State> = stateAtTimestamp.tailMap(timestamp, false).values.toList()

    fun getNthEntry(n: Int): Pair<T, State>? {
        var i = 0
        for (entry in stateAtTimestamp) {
            if (i == n) return Pair(entry.key, entry.value)
            i++
        }
        return null
    }

    override fun toString(): String =
        "CaseAnimation(caseIdentifier='$caseIdentifier', stateAtTimestamp=$stateAtTimestamp, totalAmountOfEvents=$totalAmountOfEvents)"
}
