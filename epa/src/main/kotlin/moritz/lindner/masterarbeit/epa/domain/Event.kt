package moritz.lindner.masterarbeit.epa.domain

/**
 * Represents a single event in a process instance (trace), capturing the activity performed,
 * the time it occurred, the case it belongs to, and optionally its direct predecessor and successor events.
 *
 * This class is generic in [T] to allow flexibility in timestamp formats (e.g., [Long], [java.time.LocalDateTime]).
 *
 * @param T The type used for timestamps, which must be [Comparable].
 * @property activity The activity performed in this event.
 * @property timestamp The time at which this event occurred.
 * @property caseIdentifier A unique identifier for the case (process instance) to which this event belongs.
 * @property predecessorIndex The index of the immediately preceding event within the same case, if any.
 * @property successorIndex The index of the immediately following event within the same case, if any.
 */
data class Event<T : Comparable<T>>(
    val activity: Activity,
    val timestamp: T,
    val caseIdentifier: String,
)
