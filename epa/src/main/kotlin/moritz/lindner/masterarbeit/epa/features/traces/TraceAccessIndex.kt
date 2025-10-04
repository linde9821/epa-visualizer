package moritz.lindner.masterarbeit.epa.features.traces

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class TraceAccessIndex<T : Comparable<T>> : AutomatonVisitor<T> {

    private val allEvents = mutableListOf<Event<T>>()

    private lateinit var eventsByCaseId: Map<String, List<Event<T>>>
    private lateinit var caseIdByEvent: Map<Event<T>, String>

    fun getEventsByCase(): Map<String, List<Event<T>>> {
        return eventsByCaseId
    }

    fun getTraceByEvent(event: Event<T>): List<Event<T>> {
        return eventsByCaseId[event.caseIdentifier]!!
    }

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        eventsByCaseId = allEvents
            .groupBy { it.caseIdentifier }
            .mapValues { (_, events) -> events.sortedBy(Event<T>::timestamp) }
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        event: Event<T>,
        depth: Int
    ) {
        allEvents.add(event)
    }
}