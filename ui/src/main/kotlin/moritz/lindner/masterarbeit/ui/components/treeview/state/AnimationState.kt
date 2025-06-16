package moritz.lindner.masterarbeit.ui.components.treeview.state

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.animation.TimedState

data class AnimationState(
    val time: Long,
    val current: Set<TimedState<Long>>,
) {
    private val currentStates = current.map { it.state }.toSet()
    val timedStateByState = current.associateBy { it.state }

    fun contains(state: State): Boolean = currentStates.contains(state)

    companion object {
        val Empty = AnimationState(0L, emptySet())
    }
}
