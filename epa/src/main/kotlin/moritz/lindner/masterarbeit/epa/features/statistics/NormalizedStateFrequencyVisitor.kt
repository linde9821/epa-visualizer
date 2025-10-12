package moritz.lindner.masterarbeit.epa.features.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

/**
 * Computes the normalized frequency of events per [State] in an [ExtendedPrefixAutomaton].
 *
 * The frequency is calculated as the number of events associated with a state,
 * divided by the total number of events across all states. The result is a value in [0.0, 1.0].
 *
 * The root state ([State.Root]) is always assigned a frequency of 1.0.
 * This visitor must be applied via [ExtendedPrefixAutomaton.acceptDepthFirst] or [acceptBreadthFirst]
 * before accessing any frequencies.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class NormalizedStateFrequencyVisitor<T : Comparable<T>>(
    private val progressCallback: EpaProgressCallback? = null
) : AutomatonVisitor<T> {
    private val eventsByState = HashMap<State, Set<Event<T>>>()
    private val relativeFrequencyByState = HashMap<State, Float>()
    private var totalEventCount = 0
    private val setOfTraceIdentifiers = mutableSetOf<String>()

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        val totalTraces = setOfTraceIdentifiers.size

        eventsByState.forEach { (state, eventsSeen) ->
            val tracesSeen = eventsSeen.map { it.caseIdentifier }.toSet().size

            when (state) {
                is State.PrefixState -> {
                    relativeFrequencyByState[state] = tracesSeen.toFloat() / totalTraces
                }

                else -> {

                }
            }
        }

        val max = relativeFrequencyByState.values.max()
        relativeFrequencyByState[State.Root] = max
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int,
    ) {
        eventsByState.computeIfAbsent(state) {
            extendedPrefixAutomaton.sequence(state)
        }
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        event: Event<T>,
        depth: Int
    ) {
        setOfTraceIdentifiers.add(event.caseIdentifier)
    }

    override fun onProgress(current: Long, total: Long) {
        progressCallback?.onProgress(current, total, "Compute normalized frequency of events per State")
    }

    fun build(): NormalizedStateFrequency {
        return NormalizedStateFrequency(relativeFrequencyByState)
    }
}
