package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequencyVisitor

/**
 * Filters an [ExtendedPrefixAutomaton] by removing all states whose normalized visit frequency
 * is below a given [threshold].
 *
 * The frequency is determined using a [NormalizedStateFrequencyVisitor], and only states meeting the threshold
 * (or the [State.Root]) are retained. Orphaned states with missing predecessors are also removed.
 *
 * @param T The timestamp type used in the automaton's events.
 * @property threshold The minimum normalized frequency a state must have to be retained.
 */
class StateFrequencyFilter<T : Comparable<T>>(
    private val threshold: Float,
) : EpaFilter<T> {

    override val name: String
        get() = "State Frequency Filter"

    /**
     * Applies the state frequency filter to the automaton.
     *
     * @param epa The automaton to filter.
     * @return A new [ExtendedPrefixAutomaton] with only high-frequency states and valid transitions.
     */
    override fun apply(epa: ExtendedPrefixAutomaton<T>): ExtendedPrefixAutomaton<T> {
        val normalizedStateFrequencyVisitor = NormalizedStateFrequencyVisitor<T>()
        epa.copy().acceptDepthFirst(normalizedStateFrequencyVisitor)

        val statesWithAllowedActivities =
            epa.states
                .filter { state ->
                    when (state) {
                        is State.PrefixState -> normalizedStateFrequencyVisitor.frequencyByState(state) >= threshold
                        State.Root -> true
                    }
                }.toSet()

        // remove orphans
        val filteredStates =
            statesWithAllowedActivities
                .filter { state ->
                    when (state) {
                        is State.PrefixState -> state.from in statesWithAllowedActivities
                        State.Root -> true
                    }
                }.toSet()

        val filteredActivities =
            epa.activities
                .filter { activity ->
                    filteredStates.any { state ->
                        when (state) {
                            is State.PrefixState -> state.via == activity
                            State.Root -> false
                        }
                    }
                }.toSet()

        val filteredTransitions =
            epa.transitions
                .filter { transition ->
                    transition.activity in filteredActivities &&
                        transition.start in filteredStates &&
                        transition.end in filteredStates
                }.toSet()

        val partitionByState = filteredStates.associateWith { state -> epa.partition(state) }
        val sequenceByState = filteredStates.associateWith { state -> epa.sequence(state) }

        return ExtendedPrefixAutomaton(
            eventLogName = epa.eventLogName,
            states = filteredStates,
            activities = filteredActivities,
            transitions = filteredTransitions,
            partitionByState = partitionByState,
            sequenceByState = sequenceByState,
        )
    }
}
