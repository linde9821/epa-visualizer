package moritz.lindner.masterarbeit.epa.domain

sealed class State(
    val name: String,
) : Comparable<State> {
    override fun compareTo(other: State): Int = this.name.compareTo(other.name)

    data object Root : State("root") {
        override fun toString() = name
    }

    // TODO: hashing is kind of hard because of deep recursion
    class PrefixState(
        val from: State,
        val via: Activity,
    ) : State(via.name) {
        override fun toString(): String = "$from -> $via"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PrefixState

            if (from != other.from) return false
            if (via != other.via) return false

            return true
        }

        override fun hashCode(): Int {
            var result = from.hashCode()
            result = 31 * result + via.hashCode()
            return result
        }
    }
}
