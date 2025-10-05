package moritz.lindner.masterarbeit.epa.features.traces

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class TraceAccessIndex<T> : AutomatonVisitor<T>
        where T : Comparable<T> {

    private val allEvents = mutableListOf<Event<T>>()

    private lateinit var traceByCaseId: Map<String, List<Event<T>>>

    fun getEventsByCase(): Map<String, List<Event<T>>> {
        return traceByCaseId
    }

    // can handle compressed
    fun getCycleTimeOfState(
        currentEvent: Event<T>,
        sequence: Set<Event<T>>,
        state: State
    ): T? {
        val trace = traceByCaseId[currentEvent.caseIdentifier]!!

        val traceAtState = trace
            .dropWhile { event -> !(sequence.contains(event)) }
            .dropLastWhile { event -> !(sequence.contains(event)) }

        if (traceAtState.last().successorIndex == null) {
            return null
        } else {
            val start = traceAtState.first().timestamp
            val indexOfNext = traceAtState.last().successorIndex!!
            val next = trace.find { it.predecessorIndex == traceAtState.last().successorIndex!! - 1 }!!
            val end = next.timestamp
//            return end - start
        }
        TODO()
    }

    fun getTraceByEvent(event: Event<T>): List<Event<T>> {
        return traceByCaseId[event.caseIdentifier]!!
    }

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        traceByCaseId = allEvents
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