package moritz.lindner.masterarbeit.epa.domain

/**
 * Represents a domain-specific activity within a process model.
 *
 * This is a lightweight inline value class that wraps a [String] name to
 * provide type safety and clarity when referring to activities within
 * states and transitions.
 *
 * Being a [@JvmInline] class, it has no runtime overhead and behaves like
 * a [String] in performance-sensitive contexts.
 *
 * @property name The name of the activity.
 */
@JvmInline
value class Activity(
    val name: String,
) {
    /** Returns the activity name as a string. */
    override fun toString(): String = name
}
