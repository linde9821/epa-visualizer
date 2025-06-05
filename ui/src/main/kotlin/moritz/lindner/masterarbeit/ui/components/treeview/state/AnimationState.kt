package moritz.lindner.masterarbeit.ui.components.treeview.state

import moritz.lindner.masterarbeit.epa.domain.State

data class AnimationState(
    val current: List<State>,
    val previous: List<State>,
    val upComing: List<State>,
)
