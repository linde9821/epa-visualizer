package moritz.lindner.masterarbeit.epa.features.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
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
class NormalizedStateFrequencyVisitor<T : Comparable<T>> : AutomatonVisitor<T> {
    private val eventsByState = HashMap<State, Int>()
    private val relativeFrequencyByState = HashMap<State, Float>()

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        val totalEvents = eventsByState.values.sum().toFloat()

        eventsByState.forEach { (state, eventsSeen) ->
            when (state) {
                is State.PrefixState -> {
                    relativeFrequencyByState[state] = eventsSeen.toFloat() / totalEvents
                }

                State.Root -> relativeFrequencyByState[state] = 1f
            }
        }
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int,
    ) {
        eventsByState.computeIfAbsent(state) {
            extendedPrefixAutomaton.sequence(state).size
        }
    }

    fun build(): NormalizedStateFrequency {
        return NormalizedStateFrequency(relativeFrequencyByState)
    }
}

