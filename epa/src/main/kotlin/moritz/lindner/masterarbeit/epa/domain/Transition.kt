package moritz.lindner.masterarbeit.epa.domain

/**
 * Represents a directed transition (edge) between two [State]s in an
 * Extended Prefix Automaton (EPA).
 *
 * @property start The origin [State] of the transition.
 * @property activity The [Activity] label that triggers the transition.
 * @property end The destination [State] reached after the activity is
 *    executed.
 */
data class Transition(
    val start: State,
    val activity: Activity,
    val end: State,
)
