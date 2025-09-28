package moritz.lindner.masterarbeit.epa.construction.builder.xes

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.construction.parser.EPAXesParser
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import org.deckfour.xes.`in`.XesXmlParser
import java.io.File

/**
 * Builder class for constructing an [moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton] from an XES event log file.
 *
 * This builder takes care of parsing the XES file, mapping it to domain-specific [moritz.lindner.masterarbeit.epa.domain.Event]s using a provided
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
     * Sets the [EventLogMapper] used to convert XES [XEvent]s into domain-specific [moritz.lindner.masterarbeit.epa.domain.Event]s.
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
     * Builds the [moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton] using the configured file and mapper.
     *
     * This method performs several steps:
     * - Parses the XES file
     * - Maps each event using the configured [EventLogMapper]
     * - constructs extended prefix automaton
     *
     * @throws IllegalArgumentException if required configuration is missing or the file cannot be parsed.
     * @return A fully constructed [moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton] instance.
     */
    fun build(): ExtendedPrefixAutomaton<T> {
        require(eventLogMapper != null) { "plainEventLog cannot be null" }
        require(file != null) { "file cannot be null" }
        require(file!!.exists()) { "file ${file?.absolutePath} must exist" }
        require(parser.canParse(file!!)) { "file can't be parsed" }

        val progressInputStream = ProgressInputStream(
            file!!.inputStream(),
            file!!.length(),
            { bytesRead, totalSize, _ ->
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

                    val newState = State.PrefixState(predecessorState, event.activity)
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