package moritz.lindner.masterarbeit.epa.features.animation

import moritz.lindner.masterarbeit.epa.domain.State

/**
 * Represents a time interval during which a [State] is considered active
 * within a log animation.
 *
 * This is used to model the lifecycle of states as they appear and
 * disappear over time in either a single trace (case) or across an entire
 * event log.
 *
 * @param T The timestamp type, which must be comparable (e.g., [Long],
 *    [Int], [LocalDateTime]).
 * @property state The [State] that is active during this interval.
 * @property startTime The timestamp at which the state becomes active.
 * @property endTime The timestamp at which the state becomes inactive. If
 *    `null`, the state is assumed to still be active.
 */
data class TimedState<T : Comparable<T>>(
    val state: State,
    val startTime: T,
    var endTime: T? = null, // null means still active
    val nextState: State? = null,
)
