package moritz.lindner.masterarbeit.epa.domain

sealed class State(
    val name: String,
) {
    data object Root : State("root") {
        override fun toString() = name
    }

    class PrefixState(
        val from: State,
        val via: Activity,
    ) : State(via.name) {
        override fun toString(): String = "$from -> $via"

        override fun hashCode(): Int = from.hashCode() + via.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PrefixState

            if (from != other.from) return false
            if (via != other.via) return false

            return true
        }
    }
}
