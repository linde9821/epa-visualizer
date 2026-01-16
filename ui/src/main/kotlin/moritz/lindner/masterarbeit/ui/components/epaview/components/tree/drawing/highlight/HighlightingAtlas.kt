package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.highlight

import moritz.lindner.masterarbeit.epa.domain.State

data class HighlightingAtlas(
    val pathFromRootStates: Set<State> = emptySet(),
    val selectedState: State? = null,
    val outgoingPathsState: Set<State> = emptySet(),
    val sameActivityStates: Set<State> = emptySet(),
) {
    fun withPathFromRootStates(states: Set<State>) = copy(pathFromRootStates = states)
    fun withOutgoingPathsStates(states: Set<State>) = copy(outgoingPathsState = states)
    fun withSameActivityStates(states: Set<State>) = copy(sameActivityStates = states)
}