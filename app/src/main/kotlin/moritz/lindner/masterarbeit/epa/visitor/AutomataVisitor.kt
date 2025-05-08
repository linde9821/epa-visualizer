package moritz.lindner.masterarbeit.epa.visitor

import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

interface AutomataVisitor<T : Comparable<T>> {
    fun visit(
        state: State,
        depth: Int,
    )

    fun visit(
        transition: Transition,
        depth: Int,
    )

    fun visit(
        event: Event<T>,
        depth: Int,
    )

    fun onProgress(
        current: Long,
        total: Long,
    ) {
    }
}
