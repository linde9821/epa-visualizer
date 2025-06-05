package moritz.lindner.masterarbeit.epa.visitor.case

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

class CaseVisitor<T : Comparable<T>> : AutomataVisitor<T> {
    val eventsByCase = hashMapOf<String, List<Event<T>>>()
    val cases = mutableSetOf<String>()

    override fun onEnd(extendedPrefixAutomata: ExtendedPrefixAutomata<T>) {
        eventsByCase.keys.forEach {
            cases.add(it)
        }
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        event: Event<T>,
        depth: Int,
    ) {
        eventsByCase.merge(event.caseIdentifier, listOf(event)) { a, b ->
            a + b
        }

        cases.add(event.caseIdentifier)
    }
}
