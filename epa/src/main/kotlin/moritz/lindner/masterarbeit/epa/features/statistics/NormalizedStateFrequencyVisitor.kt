package moritz.lindner.masterarbeit.epa.features.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

/**
 * Computes the normalized frequency of events per [State] in an [ExtendedPrefixAutomata].
 *
 * The frequency is calculated as the number of events associated with a state,
 * divided by the total number of events across all states. The result is a value in [0.0, 1.0].
 *
 * The root state ([State.Root]) is always assigned a frequency of 1.0.
 * This visitor must be applied via [ExtendedPrefixAutomata.acceptDepthFirst] or [acceptBreadthFirst]
 * before accessing any frequencies.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class NormalizedStateFrequencyVisitor<T : Comparable<T>> : AutomataVisitor<T> {
    private val eventsByState = HashMap<State, Int>()
    private val relativeFrequencyByState = HashMap<State, Float>()

    /**
     * Returns the normalized frequency for the given [state].
     *
     * @param state The state whose frequency to retrieve.
     * @return The normalized frequency as a float between 0.0 and 1.0.
     * @throws NullPointerException if the state was not visited or processed.
     */
    fun frequencyByState(state: State): Float = relativeFrequencyByState[state]!!

    override fun onEnd(extendedPrefixAutomata: ExtendedPrefixAutomata<T>) {
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
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {
        eventsByState.computeIfAbsent(state) {
            extendedPrefixAutomata.sequence(state).size
        }
    }

    /**
     * Returns the minimum normalized frequency across all states.
     */
    fun min(): Float = relativeFrequencyByState.values.min()

    /**
     * Returns the maximum normalized frequency across all states.
     */
    fun max(): Float = relativeFrequencyByState.values.max()
}
