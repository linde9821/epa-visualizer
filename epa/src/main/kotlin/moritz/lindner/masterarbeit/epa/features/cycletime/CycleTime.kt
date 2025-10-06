package moritz.lindner.masterarbeit.epa.features.cycletime

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class CycleTime<T : Comparable<T>> : AutomatonVisitor<T> {
    private val allEvents = mutableListOf<Event<T>>()

    private lateinit var traceByCaseId: Map<String, List<Event<T>>>
    private lateinit var extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>

    override fun onStart(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        this.extendedPrefixAutomaton = extendedPrefixAutomaton
    }

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        traceByCaseId = allEvents
            .groupBy { it.caseIdentifier }
            .mapValues { (_, events) -> events.sortedBy(Event<T>::timestamp) }
    }

    // cycle times dont work properly when chain compression is, last states can have a cycle time
    fun cycleTimesOfState(state: State, minus: (T, T) -> T): List<T> {
        val seq = extendedPrefixAutomaton.sequence(state)

        val seqWithoutChains = seq
            .groupBy { event -> event.caseIdentifier }
            .mapValues { (_, events) ->
                // get first Event accept when last event of chain is terminating (cant be filtered in the other pass)
                val lastEvent = events.maxByOrNull { it.timestamp }!!
                if (traceByCaseId[lastEvent.caseIdentifier]?.last() == lastEvent) null
                else events.minBy(Event<T>::timestamp)
            }.values.filterNotNull()

        return seqWithoutChains.mapNotNull { event ->
            cycleTimeOfEventInTrace(event, state, minus)
        }
    }

    fun cycleTimeOfEventInTrace(event: Event<T>, state: State, minus: (T, T) -> T): T? {
        val next = getNextEventInTraceAtDifferentState(event, state) ?: return null
        return minus(next.timestamp, event.timestamp)
    }

    tailrec fun getNextEventInTraceAtDifferentState(start: Event<T>, state: State): Event<T>? {
        val trace = traceByCaseId[start.caseIdentifier]!!
        val index = trace.indexOf(start)
        if (index + 1 >= trace.size) return null
        val next = trace[index + 1]
        return if (next in extendedPrefixAutomaton.sequence(state)) {
            getNextEventInTraceAtDifferentState(next, state)
        } else next
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        event: Event<T>,
        depth: Int
    ) {
        allEvents.add(event)
    }
}