package moritz.lindner.masterarbeit.epa.features.cycletime

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class CycleTimes<T : Comparable<T>> : AutomatonVisitor<T> {
    private val allEvents = mutableListOf<Event<T>>()

    private lateinit var traceByCaseId: Map<String, List<Event<T>>>
    private lateinit var extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>

    private val indexOfEventInTraceByEvent: Map<Event<T>, Int> by lazy {
        traceByCaseId.values.flatMap { trace ->
            trace.mapIndexed { index, event -> event to index }
        }.toMap()
    }

    override fun onStart(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        this.extendedPrefixAutomaton = extendedPrefixAutomaton
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        event: Event<T>,
        depth: Int
    ) {
        allEvents.add(event)
    }

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        traceByCaseId = allEvents
            .groupBy { it.caseIdentifier }
            .mapValues { (_, events) -> events.sortedBy(Event<T>::timestamp) }
    }

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

    fun cycleTimeOfEventInTrace(current: Event<T>, state: State, minus: (T, T) -> T): T? {
        val next = getNextEventInTraceAtDifferentState(current, state) ?: return null
        return minus(next.timestamp, current.timestamp)
    }

    /*
    Returns the next event in the given trace which is occuring at a state different to the state provided in the parameter
     */
    tailrec fun getNextEventInTraceAtDifferentState(current: Event<T>, state: State): Event<T>? {
        val trace = traceByCaseId[current.caseIdentifier]!!
        val index = indexOfEventInTraceByEvent[current]!!

        if (index + 1 >= trace.size) return null

        val nextEvent = trace[index + 1]
        return if (nextEvent in extendedPrefixAutomaton.sequence(state)) {
            getNextEventInTraceAtDifferentState(nextEvent, state)
        } else nextEvent
    }
}