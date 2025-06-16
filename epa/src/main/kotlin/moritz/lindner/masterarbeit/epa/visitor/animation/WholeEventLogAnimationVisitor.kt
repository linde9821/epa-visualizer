package moritz.lindner.masterarbeit.epa.visitor.animation

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor
import java.util.TreeMap

data class TimedState<T : Comparable<T>>(
    val state: State,
    val from: T,
    var to: T? = null, // null means still active
)

class WholeEventLogAnimationVisitor<T : Comparable<T>>(
    private val name: String,
) : AutomataVisitor<T> {
    private val eventsByCase = mutableMapOf<String, TreeMap<T, State>>()

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {
        extendedPrefixAutomata.sequence(state).forEach { event ->
            eventsByCase.getOrPut(event.caseIdentifier) { TreeMap() }[event.timestamp] = state
        }
    }

    fun build(): EventLogAnimation<T> {
        val timedStates = mutableListOf<TimedState<T>>()

        eventsByCase.values.forEach { timestampStateMap ->
            val entries = timestampStateMap.entries.toList()

            entries.forEachIndexed { index, (from, state) ->
                val to =
                    entries.getOrNull(index + 1)?.key
                        ?: from // maybe add epsilon padding (from + 1 as T)

                timedStates += TimedState(state = state, from = from, to = to)
            }
        }

        return EventLogAnimation(name, timedStates.sortedBy { it.from }, timedStates.size)
    }
}
