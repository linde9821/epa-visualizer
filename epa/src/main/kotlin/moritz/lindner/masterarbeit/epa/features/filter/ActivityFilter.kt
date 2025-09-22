package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.State

/**
 * A filter that removes all states, transitions, and events from an [ExtendedPrefixAutomaton]
 * that are not associated with the specified set of allowed [Activity]s.
 *
 * This filter preserves only those parts of the automaton that:
 * - Are reachable via allowed activities
 * - Maintain valid transition chains from the root
 *
 * Orphaned states (those whose path includes disallowed activities) are pruned.
 *
 * @param T The timestamp type used in the automaton's events.
 * @property allowedActivities The set of activity labels to retain in the filtered automaton.
 */
class ActivityFilter<T : Comparable<T>>(
    private val allowedActivities: HashSet<Activity>,
) : EpaFilter<T> {

    override val name = "Activity filter"

    /**
     * Applies the activity-based filtering logic to the given automaton.
     *
     * @param epa The automaton to filter.
     * @return A new [ExtendedPrefixAutomaton] that includes only allowed activities and valid state chains.
     */
    override fun apply(epa: ExtendedPrefixAutomaton<T>): ExtendedPrefixAutomaton<T> {
        val statesWithAllowedActivities =
            epa.states
                .filter { state ->
                    when (state) {
                        is State.PrefixState -> state.via in allowedActivities
                        State.Root -> true
                    }
                }.toSet()

        // remove orphans
        val filteredStates =
            statesWithAllowedActivities
                .filter { state ->
                    when (state) {
                        is State.PrefixState -> chainIsValid(state)
                        State.Root -> true
                    }
                }.toSet()

        val filteredActivities = epa.activities.filter { activity -> allowedActivities.contains(activity) }.toSet()

        val filteredTransitions =
            epa.transitions
                .filter { transition ->
                    transition.activity in allowedActivities &&
                            transition.start in filteredStates &&
                            transition.end in filteredStates
                }.toSet()

        val partitionByState = filteredStates.associateWith(epa::partition)
        val sequenceByState = filteredStates.associateWith(epa::sequence)

        return ExtendedPrefixAutomaton(
            eventLogName = epa.eventLogName,
            states = filteredStates,
            activities = filteredActivities,
            transitions = filteredTransitions,
            partitionByState = partitionByState,
            sequenceByState = sequenceByState,
        )
    }

    // TODO: check this works more thoroughly
    private fun chainIsValid(state: State.PrefixState): Boolean =
        if (state.via in allowedActivities) {
            when (state.from) {
                is State.PrefixState -> chainIsValid(state.from)
                State.Root -> true
            }
        } else {
            false
        }
}
