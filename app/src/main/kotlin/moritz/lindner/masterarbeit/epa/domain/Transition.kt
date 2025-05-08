package moritz.lindner.masterarbeit.epa.domain

data class Transition(
    val start: State,
    val activity: Activity,
    val end: State,
)
