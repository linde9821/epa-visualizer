package moritz.lindner.masterarbeit.epa.features.animation

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

/**
 * A visitor that extracts a single trace (case) from an
 * [ExtendedPrefixAutomaton] and builds an [EventLogAnimation]
 * representing the state transitions over time for that specific case.
 *
 * @param T The timestamp type used in the associated events (must be
 *    comparable, e.g. Long, Int, LocalDateTime).
 * @property caseIdentifier The unique identifier of the case to extract
 *    and animate.
 */
class SingleCaseAnimationBuilder<T : Comparable<T>>(
    val caseIdentifier: String,
) : AutomatonVisitor<T> {
    private val events = mutableListOf<Pair<T, State>>()

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int,
    ) {
        extendedPrefixAutomaton.sequence(state).forEach { event ->
            if (event.caseIdentifier == caseIdentifier) {
                events += event.timestamp to state
            }
        }
    }

    /**
     * Builds an [EventLogAnimation] for the given case, with interpolable
     * [TimedState]s including both the current and next state for smooth
     * animation.
     */
    fun build(): EventLogAnimation<T> {
        val sorted = events.sortedBy { it.first }
        val timedStates = mutableListOf<TimedState<T>>()

        sorted.forEachIndexed { index, (from, state) ->
            val toEntry = sorted.getOrNull(index + 1)
            val to = toEntry?.first ?: from // fallback to `from` if last
            val nextState = toEntry?.second

            timedStates +=
                TimedState(
                    state = state,
                    startTime = from,
                    endTime = to,
                    nextState = nextState,
                )
        }

        return EventLogAnimation(caseIdentifier, timedStates.sortedBy { it.startTime }, timedStates.size)
    }
}
