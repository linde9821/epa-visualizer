package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaFromComponentsBuilder
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
    allowedActivities: HashSet<Activity>,
) : EpaFilter<T> {

    override val name = "Activity filter"

    val allowedActivities = allowedActivities.map { it.name }.toSet()


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
                        is State.PrefixState -> state.via.name in allowedActivities
                        State.Root -> true
                    }
                }.toSet()

        val builder = EpaFromComponentsBuilder<T>()
            .fromExisting(epa)
            .setStates(statesWithAllowedActivities)
            .setActivities(epa.activities)
            .pruneStatesUnreachableByTransitions(true)

        return builder.build()
    }

}
