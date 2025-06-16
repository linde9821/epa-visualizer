package moritz.lindner.masterarbeit.epa.visitor.animation

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

class SingleCaseAnimationVisitor<T : Comparable<T>>(
    val caseIdentifier: String,
) : AutomataVisitor<T> {
    private val events = mutableListOf<Pair<T, State>>()

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {
        extendedPrefixAutomata.sequence(state).forEach { event ->
            if (event.caseIdentifier == caseIdentifier) {
                events += event.timestamp to state
            }
        }
    }

    fun build(): EventLogAnimation<T> {
        val sorted = events.sortedBy { it.first }
        val timedStates = mutableListOf<TimedState<T>>()

        sorted.forEachIndexed { index, (from, state) ->
            val to = sorted.getOrNull(index + 1)?.first ?: from // final state's interval ends at its own time
            timedStates += TimedState(state, from, to)
        }

        return EventLogAnimation(caseIdentifier, timedStates, timedStates.size)
    }
}
