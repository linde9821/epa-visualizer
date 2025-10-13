package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.highlight

import moritz.lindner.masterarbeit.epa.domain.State

data class HighlightingAtlas(
    val highlightedStates: Set<State> = emptySet(),
    val selectedState: State? = null,
) {
    fun withHighlightedState(states: Set<State>) = copy(highlightedStates = highlightedStates + states)
    fun withoutHighlightedState(states: Set<State>) = copy(highlightedStates = highlightedStates - states)
    fun selectedState(state: State) = copy(selectedState = state)
}