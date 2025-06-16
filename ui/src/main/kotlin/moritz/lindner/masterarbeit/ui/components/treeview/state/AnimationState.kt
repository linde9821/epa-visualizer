package moritz.lindner.masterarbeit.ui.components.treeview.state

import moritz.lindner.masterarbeit.epa.domain.State

data class AnimationState(
    val current: Set<State>,
) {
    companion object {
        val Empty = AnimationState(emptySet())
    }
}
