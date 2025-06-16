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
 * @param T The timestamp type (must be comparable, e.g., [Long], [Int], [LocalDateTime]).
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

    @Suppress("UNCHECKED_CAST")
    fun increment(value: T): T =
        when (value) {
            is Long -> (value + 100_000L) as T
            is Int -> (value + 1) as T
            is Double -> (value + 10.0) as T
            else -> value // no-op fallback
        }

    /**
     * Builds the [EventLogAnimation] by converting each case's sorted state timeline
     * into a list of [TimedState]s with defined [from] and [to] intervals.
     *
     * Final states (last in their trace) are closed by using the same timestamp for [to],
     * or optionally extended using a fixed epsilon offset (see note below).
     *
     * @return A unified [EventLogAnimation] covering all cases in the log.
     */
    fun build(): EventLogAnimation<T> {
        val timedStates = mutableListOf<TimedState<T>>()

        eventsByCase.values.forEach { timestampStateMap ->
            val entries = timestampStateMap.entries.toList()

            entries.forEachIndexed { index, (from, state) ->
                val to = entries.getOrNull(index + 1)?.key ?: increment(from)
                val nextState = entries.getOrNull(index + 1)?.value

                timedStates +=
                    TimedState(
                        state = state,
                        from = from,
                        to = to,
                        nextState = nextState,
                    )
            }
        }

        return EventLogAnimation(name, timedStates.sortedBy { it.from }, timedStates.size)
    }
}
