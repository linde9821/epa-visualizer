package moritz.lindner.masterarbeit.epa.features.animation

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

/**
 * A visitor implementation that collects events grouped by case identifier during automaton traversal.
 *
 * @param T The type of the timestamp used in events, which must be comparable (e.g., Int, Long, LocalDateTime).
 *
 * @property eventsByCase A mapping from each case identifier to the list of its associated events.
 * @property cases A set of all distinct case identifiers encountered during traversal.
 */
class EventsByCasesCollector<T : Comparable<T>> : AutomatonVisitor<T> {
    /** Maps each case identifier to the list of [Event]s associated with it. */
    val eventsByCase = hashMapOf<String, List<Event<T>>>()

    /** The set of all unique case identifiers seen during traversal. */
    private val cases = mutableSetOf<String>()

    fun build(): Map<String, List<Event<T>>> {
        return eventsByCase
    }

    /**
     * Called before the automaton traversal begins. Currently, does nothing.
     */
    override fun onStart(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {

    }

    /**
     * Called after the automaton traversal ends. Currently, ensures all keys from [eventsByCase]
     * are reflected in [cases], which may be useful for consistency if `visit` wasn't called.
     */
    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        cases.addAll(eventsByCase.keys)
    }

    /**
     * Visits an event during automaton traversal and groups it by its [caseIdentifier].
     *
     * @param extendedPrefixAutomaton The automaton being visited.
     * @param event The event encountered at the current node.
     * @param depth The depth in the automaton (ignored here).
     */
    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        event: Event<T>,
        depth: Int,
    ) {
        eventsByCase.merge(event.caseIdentifier, listOf(event)) { existing, new ->
            existing + new
        }
        cases.add(event.caseIdentifier)
    }
}
