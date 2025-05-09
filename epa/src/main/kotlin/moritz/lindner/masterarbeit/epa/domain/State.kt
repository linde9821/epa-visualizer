package moritz.lindner.masterarbeit.epa.domain

sealed class State {
    data object Root : State()

    data class PrefixState(
        val from: State,
        val via: Activity,
    ) : State() {
        override fun toString(): String = "$from -> $via"
    }
}
