package moritz.lindner.masterarbeit.epa.domain

sealed class State(
    val name: String,
) {
    data object Root : State("root") {
        override fun toString() = name
    }

    class PrefixState(
        name: String,
        val from: State,
        val via: Activity,
    ) : State(name) {
        override fun toString(): String = "$from -> $via"
    }
}
