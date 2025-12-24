package moritz.lindner.masterarbeit.epa.features.cycletime

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
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
            .groupBy(Event<T>::caseIdentifier)
            .mapValues { (_, events) -> events.sortedBy(Event<T>::timestamp) }
    }

    fun cycleTimeOfTransition(
        state: State,
        transitions: List<Transition>,
        minus: (T, T) -> T
    ): Map<Transition, List<T>> {
        require(transitions.all { it.start == state })

        val seq = extendedPrefixAutomaton.sequence(state)

        return buildMap {
            transitions.forEach { transition ->
                val cts = seq.map { event ->
                    val nextEventInTraceAtSpecificState = getNextEventInTraceAtSpecificState(
                        current = event,
                        state = state,
                        targetState = transition.end
                    )
                    Pair(event, nextEventInTraceAtSpecificState)
                }.filter { (_, successor) -> successor != null }
                    .map { (event, successor) ->
                        minus(successor!!.timestamp, event.timestamp)
                    }

                put(transition, cts)
            }
        }
    }

    fun averageCycleTimesOfState(state: State, minus: (T, T) -> T): List<T> {
        val seq = extendedPrefixAutomaton.sequence(state)

        return seq
            .map { event ->
                val successor = getNextEventInTraceAtDifferentState(
                    current = event,
                    state = state
                )
                Pair(event, successor)
            }.filter { (_, successor) ->
                successor != null
            }.map { (event, successor) ->
                minus(successor!!.timestamp, event.timestamp)
            }
    }

    /*
    Returns the next event in the given trace which is occurring at a state different to the state provided in the parameter
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

    tailrec fun getNextEventInTraceAtSpecificState(current: Event<T>, state: State, targetState: State): Event<T>? {
        val trace = traceByCaseId[current.caseIdentifier]!!
        val index = indexOfEventInTraceByEvent[current]!!

        if (index + 1 >= trace.size) return null

        val nextEvent = trace[index + 1]

        return if (nextEvent in extendedPrefixAutomaton.sequence(targetState)) {
            nextEvent
        } else getNextEventInTraceAtSpecificState(nextEvent, state, targetState)
    }
}