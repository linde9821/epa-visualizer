package moritz.lindner.masterarbeit.ui.components.treeview.state

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.animation.TimedState

data class AnimationState(
    val time: Long,
    val currentTimeStates: Set<TimedState<Long>>,
) {
    private val currentStates = currentTimeStates.map { it.state }.toSet()

    // O(1)
    fun contains(state: State): Boolean = currentStates.contains(state)

    companion object {
        val Empty = AnimationState(0L, emptySet())
    }
}
