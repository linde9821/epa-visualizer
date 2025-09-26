package moritz.lindner.masterarbeit.epa.construction.builder

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.features.reachability.IsReachableVisitor

/**
 * Builder class for constructing an [moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton] from existing components.
 *
 * This builder allows creating EPA instances from pre-existing components (states, activities, transitions, etc.)
 * rather than parsing from XES files. It's particularly useful for creating modified or filtered versions of
 * existing EPAs, combining multiple EPAs, or constructing EPAs programmatically.
 *
 * The builder provides optional cleanup functionality to ensure EPA consistency by pruning unreachable states
 * and their associated data. This helps maintain valid automaton structure when working with filtered or
 * modified component sets.
 *
 * ## Key Features:
 * - Build EPA from individual components (states, transitions, activities, etc.)
 * - Copy and modify existing EPAs via [fromExisting]
 * - Automatic cleanup of unreachable states and orphaned data
 * - Progress callback support for long-running operations
 * - Comprehensive validation of required components
 */
class EpaFromComponentsBuilder<T : Comparable<T>> {

    private var pruneUnreachableStates: Boolean = true
    private var progressCallback: EpaProgressCallback? = null

    private var eventLogName: String? = null
    private var states: Set<State> = setOf()
    private var activities: Set<Activity> = setOf()
    private var transitions: Set<Transition> = setOf()
    private var partitionByState: Map<State, Int> = mapOf()
    private var sequenceByState: Map<State, Set<Event<T>>> = mapOf()

    /**
     * Sets the event log name for the EPA.
     *
     * @param name The name of the event log.
     * @return This builder instance for chaining.
     */
    fun setEventLogName(name: String): EpaFromComponentsBuilder<T> = apply {
        eventLogName = name
    }

    /**
     * Sets the complete set of states for the EPA.
     *
     * @param states The set of states to use.
     * @return This builder instance for chaining.
     */
    fun setStates(states: Set<State>): EpaFromComponentsBuilder<T> = apply {
        this.states = states
    }

    /**
     * Sets the complete set of activities for the EPA.
     *
     * @param activities The set of activities to use.
     * @return This builder instance for chaining.
     */
    fun setActivities(activities: Set<Activity>): EpaFromComponentsBuilder<T> = apply {
        this.activities = activities
    }

    /**
     * Sets the complete set of transitions for the EPA.
     *
     * @param transitions The set of transitions to use.
     * @return This builder instance for chaining.
     */
    fun setTransitions(transitions: Set<Transition>): EpaFromComponentsBuilder<T> = apply {
        this.transitions = transitions
    }

    /**
     * Sets the partition mapping for states.
     *
     * @param partitionByState Map from states to their partition numbers.
     * @return This builder instance for chaining.
     */
    fun setPartitionByState(partitionByState: Map<State, Int>): EpaFromComponentsBuilder<T> = apply {
        this.partitionByState = partitionByState
    }

    /**
     * Sets the sequence mapping for states.
     *
     * @param sequenceByState Map from states to their event sequences.
     * @return This builder instance for chaining.
     */
    fun setSequenceByState(sequenceByState: Map<State, Set<Event<T>>>): EpaFromComponentsBuilder<T> = apply {
        this.sequenceByState = sequenceByState
    }

    /**
     * Configures whether to automatically prune unreachable states and their associated information (transitions,
     * activities, partitions, sequences) during EPA construction.
     *
     * When enabled, the builder performs the following cleanup steps:
     * 1. Removes all transitions where start or end states are not in the configured state set
     * 2. Removes all states that are not reachable via the remaining valid transitions
     * 3. Removes associated data (activities, partitions, sequences) for the pruned states
     *
     * This ensures the resulting EPA contains only connected, valid components.
     *
     * @param value `true` to enable pruning of unreachable states and associated data, `false` to keep all states as-is.
     * @return This builder instance for chaining.
     */
    fun pruneStatesUnreachableByTransitions(value: Boolean): EpaFromComponentsBuilder<T> {
        pruneUnreachableStates = value
        return this
    }

    fun setProgressCallback(callback: EpaProgressCallback): EpaFromComponentsBuilder<T> {
        progressCallback = callback
        return this
    }

    fun fromExisting(epa: ExtendedPrefixAutomaton<T>) = apply {
        this.eventLogName = epa.eventLogName
        this.states = epa.states
        this.activities = epa.activities
        this.transitions = epa.transitions
        this.partitionByState = epa.states.associateWith { state -> epa.partition(state) }
        this.sequenceByState = epa.states.associateWith { state -> epa.sequence(state) }
    }

    fun build(): ExtendedPrefixAutomaton<T> {
        require(eventLogName != null) { "A eventLogName must be present" }
        require(states.isNotEmpty()) { "At least one state must be present" }
        require(states.contains(State.Root)) { "Root state must be present" }

        return if (pruneUnreachableStates) {
            progressCallback?.onProgress(0, states.size.toLong(), task = "Build Epa From components with pruning")

            val filteredTransitions = transitions
                .filter { transition ->
                    transition.start in states &&
                            transition.end in states
                }.toSet()

            val notYetPrunedEpa = ExtendedPrefixAutomaton(
                eventLogName = eventLogName!!,
                states = states,
                activities = activities,
                transitions = filteredTransitions,
                partitionByState = partitionByState,
                sequenceByState = sequenceByState,
            )

            val isReachableVisitor = IsReachableVisitor<T>(
                statesToCheckReach = notYetPrunedEpa.states,
                onProgressCallback = progressCallback
            )

            notYetPrunedEpa.acceptDepthFirst(isReachableVisitor)

            val reachableStates = notYetPrunedEpa.states.filter(isReachableVisitor::isReachable).toSet()
            val partitionsByReachableStates = reachableStates.associateWith(notYetPrunedEpa::partition)
            val sequencesByReachableStates = reachableStates.associateWith(notYetPrunedEpa::sequence)
            val reachableActivities = reachableStates.mapNotNull { state ->
                when (state) {
                    is State.PrefixState -> state.via
                    State.Root -> null
                }
            }.toSet()
            val reachableTransitions = notYetPrunedEpa.transitions.filter { transitionToCheck ->
                transitionToCheck.activity in reachableActivities &&
                        transitionToCheck.start in reachableStates &&
                        transitionToCheck.end in reachableStates
            }.toSet()

            progressCallback?.onProgress(states.size.toLong(), states.size.toLong(), task = "Build Epa From components")

            ExtendedPrefixAutomaton(
                eventLogName = eventLogName!!,
                states = reachableStates,
                activities = reachableActivities,
                transitions = reachableTransitions,
                partitionByState = partitionsByReachableStates,
                sequenceByState = sequencesByReachableStates,
            )
        } else {
            progressCallback?.onProgress(states.size.toLong(), states.size.toLong(), task = "Build Epa From components")
            ExtendedPrefixAutomaton(
                eventLogName = eventLogName!!,
                states = states,
                activities = activities,
                transitions = transitions,
                partitionByState = partitionByState,
                sequenceByState = sequenceByState,
            )
        }
    }

}