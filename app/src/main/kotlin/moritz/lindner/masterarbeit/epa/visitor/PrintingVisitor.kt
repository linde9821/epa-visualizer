package moritz.lindner.masterarbeit.epa.visitor

import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

class PrintingVisitor<T : Comparable<T>>(
    private val printState: Boolean = true,
    private val printTransition: Boolean = true,
    private val printEvent: Boolean = true,
) : AutomataVisitor<T> {
    override fun visit(
        state: State,
        depth: Int,
    ) {
        if (printState) {
            println("$state (depth = $depth)")
        }
    }

    override fun visit(
        transition: Transition,
        depth: Int,
    ) {
        if (printTransition) {
            println("$transition")
        }
    }

    override fun visit(
        event: Event<T>,
        depth: Int,
    ) {
        if (printEvent) {
            println("$event")
        }
    }
}
