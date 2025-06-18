package moritz.lindner.masterarbeit.epa.visitor.animation

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor
import java.util.TreeMap

/**
 * An [AutomataVisitor] that builds an [EventLogAnimation] for an entire event log.
 *
 * This visitor collects all state transitions across all cases (traces) in the
 * [ExtendedPrefixAutomata], associating each [State] with its timestamp and case identifier.
 *
 * For each case, it builds a sequence of [TimedState]s, where each state is active from
 * its associated event timestamp until the next one (or just at its timestamp if it's the last).
 *
 * @param T The timestamp type (must be comparable, e.g., [Long], [Int], [java.time.LocalDateTime]).
 * @property name A label identifying the source or name of the animation (e.g., log file name).
 */
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

    /**
     * Builds the [EventLogAnimation] by converting each case's sorted state timeline
     * into a list of [TimedState]s with defined [from] and [to] intervals.
     *
     * Final states (last in their trace) are closed by incrementing the `from` timestamp using
     * a provided epsilon value.
     *
     * @param epsilon The value to add to the last timestamp to close open-ended intervals.
     * @param increment A function that defines how to add [epsilon] to a [T]-typed timestamp.
     * @return A unified [EventLogAnimation] covering all cases in the log.
     */
    fun build(
        epsilon: T,
        increment: (T, T) -> T,
    ): EventLogAnimation<T> {
        val timedStates = mutableListOf<TimedState<T>>()

        eventsByCase.values.forEach { timestampStateMap ->
            val entries = timestampStateMap.entries.toList()

            entries.forEachIndexed { index, (from, state) ->
                // TODO: maybe add minimum her ()
                val to = entries.getOrNull(index + 1)?.key ?: increment(from, epsilon)
                val nextState = entries.getOrNull(index + 1)?.value

                timedStates.add(
                    TimedState(
                        state = state,
                        from = from,
                        to = to,
                        nextState = nextState,
                    ),
                )
            }
        }

        return EventLogAnimation(
            identifier = name,
            timedStates = timedStates.sortedBy { timedState -> timedState.from },
            totalAmountOfEvents = timedStates.size,
        )
    }
}
