package moritz.lindner.masterarbeit.epa.domain

/**
 * Represents a node (state) in an Extended Prefix Automaton (EPA).
 *
 * This class hierarchy supports comparing and identifying states based
 * on their prefix structure, allowing deterministic graph traversal and
 * partitioning in downstream EPA construction.
 *
 * @property name A human-readable name of the state, typically derived
 *    from the most recent activity in the path.
 */
sealed class State(
    val name: String,
) : Comparable<State> {
    /**
     * Compares states based on their name, used for consistent ordering of EPA
     * nodes.
     */
    override fun compareTo(other: State): Int = this.name.compareTo(other.name)

    /**
     * Represents the root of the EPA.
     *
     * All traces in the event log start from this node. It has no incoming
     * transitions and serves as the origin of all prefix paths.
     */
    data object Root : State("root") {
        override fun toString() = name
    }

    /**
     * Represents a state that extends a prefix in the EPA via a specific
     * activity.
     *
     * This state captures the idea of one step further in the sequence of
     * activities starting from a previous [from] state via a labeled [via]
     * activity.
     *
     * States are equal if and only if both the [from] and [via] values match,
     * allowing for recursive construction and structural comparison of the
     * prefix graph.
     *
     * @property from The preceding [State] in the prefix path.
     * @property via The [Activity] that led from [from] to this state.
     */
    class PrefixState(
        val from: State,
        val via: Activity,
    ) : State(via.name) {
        /**
         * Returns a readable representation of the prefix path, e.g., "root -> A
         * -> B".
         */
        override fun toString(): String = "[${from.name}] -> $via"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PrefixState

            if (from != other.from) return false
            if (via != other.via) return false

            return true
        }

        // TODO: hashing is kind of hard because of deep recursion
        override fun hashCode(): Int {
            var result = from.hashCode()
            result = 31 * result + via.hashCode()
            return result
        }
    }
}
