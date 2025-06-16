package moritz.lindner.masterarbeit.epa.visitor.statistics

import moritz.lindner.masterarbeit.epa.domain.Activity

/**
 * Represents summary statistics for an [ExtendedPrefixAutomata] or an event log.
 *
 * Captures global and activity-level metrics such as the number of events, cases,
 * activities, states, and partitions, as well as the frequency of each activity.
 *
 * @property eventCount Total number of events observed.
 * @property caseCount Total number of unique cases (process instances).
 * @property activityCount Number of distinct activities.
 * @property stateCount Number of unique states in the automaton.
 * @property partitionsCount Number of partitions used to group states.
 * @property activityFrequency A mapping of each [Activity] to its observed frequency.
 */
data class Statistics<T : Comparable<T>>(
    val eventCount: Int,
    val caseCount: Int,
    val activityCount: Int,
    val stateCount: Int,
    val partitionsCount: Int,
    val activityFrequency: Map<Activity, Int>,
    val interval: Pair<T, T>,
)
