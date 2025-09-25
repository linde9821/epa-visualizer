package moritz.lindner.masterarbeit.epa.construction.builder


import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.parser.EPAXesParser
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.features.reachability.IsReachableVisitor
import org.deckfour.xes.`in`.XesXmlParser
import java.io.File

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
     */    fun pruneStatesUnreachableByTransitions(value: Boolean): EpaFromComponentsBuilder<T> {
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

        if (pruneUnreachableStates) {
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
                    is PrefixState -> state.via
                    State.Root -> null
                }
            }.toSet()
            val reachableTransitions = notYetPrunedEpa.transitions.filter { transitionToCheck ->
                transitionToCheck.activity in reachableActivities &&
                        transitionToCheck.start in reachableStates &&
                        transitionToCheck.end in reachableStates
            }.toSet()

            progressCallback?.onProgress(states.size.toLong(), states.size.toLong(), task = "Build Epa From components")

            return ExtendedPrefixAutomaton(
                eventLogName = eventLogName!!,
                states = reachableStates,
                activities = reachableActivities,
                transitions = reachableTransitions,
                partitionByState = partitionsByReachableStates,
                sequenceByState = sequencesByReachableStates,
            )
        } else {
            progressCallback?.onProgress(states.size.toLong(), states.size.toLong(), task = "Build Epa From components")
            return ExtendedPrefixAutomaton(
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

/**
 * Builder class for constructing an [ExtendedPrefixAutomaton] from an XES event log file.
 *
 * This builder takes care of parsing the XES file, mapping it to domain-specific [Event]s using a provided
 * [EventLogMapper], and building the extended prefix automaton.
 *
 * @param T The timestamp type used in the event log, which must be [Comparable].
 */
class EpaFromXesBuilder<T : Comparable<T>> {
    private var eventLogMapper: EventLogMapper<T>? = null
    private var file: File? = null
    private var progressCallback: EpaProgressCallback? = null
    private val parser: XesXmlParser = EPAXesParser()

    private var nextPartition = 1

    /**
     * Sets the [EventLogMapper] used to convert XES [XEvent]s into domain-specific [Event]s.
     *
     * @param mapper The implementation of [EventLogMapper] to be used.
     * @return This builder instance for chaining.
     */
    fun setEventLogMapper(mapper: EventLogMapper<T>): EpaFromXesBuilder<T> {
        eventLogMapper = mapper
        return this
    }

    fun setProgressCallback(callback: EpaProgressCallback): EpaFromXesBuilder<T> {
        progressCallback = callback
        return this
    }

    /**
     * Sets the input XES log file to be parsed and used for building the automaton.
     *
     * @param f The input file in XES format.
     * @return This builder instance for chaining.
     */
    fun setFile(f: File): EpaFromXesBuilder<T> {
        file = f
        return this
    }

    /**
     * Builds the [ExtendedPrefixAutomaton] using the configured file and mapper.
     *
     * This method performs several steps:
     * - Parses the XES file
     * - Maps each event using the configured [EventLogMapper]
     * - constructs extended prefix automaton
     *
     * @throws IllegalArgumentException if required configuration is missing or the file cannot be parsed.
     * @return A fully constructed [ExtendedPrefixAutomaton] instance.
     */
    fun build(): ExtendedPrefixAutomaton<T> {
        require(eventLogMapper != null) { "plainEventLog cannot be null" }
        require(file != null) { "file cannot be null" }
        require(file!!.exists()) { "file must exist" }
        require(parser.canParse(file!!)) { "file can't be parsed" }

        val progressInputStream = ProgressInputStream(
            file!!.inputStream(),
            file!!.length(),
            { bytesRead, totalSize, percentage ->
                progressCallback?.onProgress(bytesRead, totalSize, "parsing xes")
            }
        )
        val log = parser.parse(progressInputStream).first()

        val plainEventLog = eventLogMapper!!.build(log, progressCallback)

        val states: MutableSet<State> = hashSetOf(State.Root)
        val transitions: MutableSet<Transition> = hashSetOf()
        val activities: MutableSet<Activity> = hashSetOf()
        val sequences: MutableMap<State, MutableSet<Event<T>>> = hashMapOf(State.Root to hashSetOf())
        val partitionByState: MutableMap<State, Int> = hashMapOf(State.Root to 0)
        val lastActivityByState: MutableMap<String, State> = hashMapOf()
        val transitionByPredecessorStateAndActivity = hashMapOf<Pair<State, Activity>, Transition>()
        val transitionByPredecessorState = hashMapOf<State, Transition>()

        plainEventLog
            .forEachIndexed { index, event ->
                progressCallback?.onProgress(
                    current = (index + 1).toLong(),
                    total = plainEventLog.size.toLong(),
                    task = "Constructing EPA"
                )
                val predecessorState = lastActivityByState[event.caseIdentifier] ?: State.Root

                val currentActivity: State?

                val existingTransition = transitionByPredecessorStateAndActivity[Pair(predecessorState, event.activity)]

                if (existingTransition != null) {
                    currentActivity = existingTransition.end
                } else {
                    val existingTransitionFromPredecessor = transitionByPredecessorState[predecessorState]

                    val c = getPartition(existingTransitionFromPredecessor, partitionByState, predecessorState)

                    val newState = PrefixState(predecessorState, event.activity)
                    states.add(newState)
                    val newTransition = Transition(predecessorState, event.activity, newState)
                    transitionByPredecessorStateAndActivity[Pair(predecessorState, event.activity)] = newTransition
                    transitionByPredecessorState[predecessorState] = newTransition
                    transitions.add(newTransition)
                    activities.add(event.activity)
                    partitionByState[newState] = c
                    currentActivity = newState
                }

                sequences.computeIfAbsent(currentActivity) { mutableSetOf() }.add(event)
                lastActivityByState[event.caseIdentifier] = currentActivity
            }

        return ExtendedPrefixAutomaton(
            eventLogName = file!!.name,
            states = states,
            activities = activities,
            transitions = transitions,
            partitionByState = partitionByState,
            sequenceByState = sequences,
        )
    }

    private fun getPartition(
        existingTransitionFromPredecessor: Transition?,
        partitionByState: Map<State, Int>,
        predecessorState: State,
    ): Int =
        if (existingTransitionFromPredecessor != null) {
            nextPartition++
            nextPartition
        } else {
            if (predecessorState == State.Root) {
                1
            } else {
                partitionByState[predecessorState]
                    ?: throw IllegalStateException("Unknown predecessor activity but there must be one")
            }
        }
}
