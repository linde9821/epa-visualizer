package moritz.lindner.masterarbeit.epa.features.animation

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor
import java.util.TreeMap

/**
 * An [AutomatonVisitor] that builds an [EventLogAnimation] for an entire
 * event log.
 *
 * This visitor collects all state transitions across all cases (traces)
 * in the [ExtendedPrefixAutomaton], associating each [State] with its
 * timestamp and case identifier.
 *
 * For each case, it builds a sequence of [TimedState]s, where each state
 * is active from its associated event timestamp until the next one (or
 * just at its timestamp if it's the last).
 *
 * @param T The timestamp type (must be comparable, e.g., [Long], [Int],
 *    [java.time.LocalDateTime]).
 * @property name A label identifying the source or name of the animation
 *    (e.g., log file name).
 */
class WholeEventLogAnimationBuilder<T : Comparable<T>>(
    private val name: String,
) : AutomatonVisitor<T> {
    // TODO: ensure this works for multiple events with same timestamp (otherwise only a transition to the last state will be shown which looks strange
    private val activeStateByCaseIdentifier = mutableMapOf<String, TreeMap<T, State>>()

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int,
    ) {
        extendedPrefixAutomaton
            .sequence(state)
            .forEach { event ->
                activeStateByCaseIdentifier.getOrPut(event.caseIdentifier) { TreeMap() }[event.timestamp] = state
            }
    }

    /**
     * Builds the [EventLogAnimation] by converting each case's sorted state
     * timeline into a list of [TimedState]s with defined [from] and [to]
     * intervals.
     *
     * Final states (last in their trace) are closed by incrementing the `from`
     * timestamp using a provided epsilon value.
     *
     * @param epsilon The value to add to the last timestamp to close
     *    open-ended intervals.
     * @param increment A function that defines how to add [epsilon] to a
     *    [T]-typed timestamp.
     * @return A unified [EventLogAnimation] covering all cases in the log.
     */
    fun build(
        epsilon: T,
        increment: (T, T) -> T,
    ): EventLogAnimation<T> {
        val timedStates = mutableListOf<TimedState<T>>()

        activeStateByCaseIdentifier.values.forEach { stateByTimestamp ->
            val timestampsAndStates = stateByTimestamp.entries.toList()

            timestampsAndStates.forEachIndexed { index, (start, state) ->
                val end = timestampsAndStates.getOrNull(index + 1)?.key ?: increment(start, epsilon)
                val nextState = timestampsAndStates.getOrNull(index + 1)?.value

                timedStates.add(
                    TimedState(
                        startTime = start,
                        endTime = end,
                        state = state,
                        nextState = nextState,
                    ),
                )
            }
        }

        return EventLogAnimation(
            identifier = name,
            timedStates = timedStates,
            totalAmountOfEvents = timedStates.size,
        )
    }
}
