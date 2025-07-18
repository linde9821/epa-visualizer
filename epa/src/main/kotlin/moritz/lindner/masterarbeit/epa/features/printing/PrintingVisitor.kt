package moritz.lindner.masterarbeit.epa.features.printing

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class PrintingVisitor<T : Comparable<T>>(
    private val printState: Boolean = true,
    private val printTransition: Boolean = true,
    private val printEvent: Boolean = true,
) : AutomatonVisitor<T> {
    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int,
    ) {
        if (printState) {
            println("$state (depth = $depth)")
        }
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        transition: Transition,
        depth: Int,
    ) {
        if (printTransition) {
            println("$transition")
        }
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        event: Event<T>,
        depth: Int,
    ) {
        if (printEvent) {
            println("$event")
        }
    }
}
