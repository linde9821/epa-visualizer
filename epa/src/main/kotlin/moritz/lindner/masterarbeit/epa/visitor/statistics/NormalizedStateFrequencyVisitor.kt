package moritz.lindner.masterarbeit.epa.visitor.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

/**
frequency of the amount of events the state has seen of a type by the total amount of the event type
 */
class NormalizedStateFrequencyVisitor<T : Comparable<T>> : AutomataVisitor<T> {
    private val eventsByState = HashMap<State, Int>()
    private val relativeFrequencyByState = HashMap<State, Float>()

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
}
