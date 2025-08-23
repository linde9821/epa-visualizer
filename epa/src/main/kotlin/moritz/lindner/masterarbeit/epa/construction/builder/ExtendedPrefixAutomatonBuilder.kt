package moritz.lindner.masterarbeit.epa.construction.builder


import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.parser.EPAXesParser
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.State.PrefixState
import moritz.lindner.masterarbeit.epa.domain.Transition
import org.deckfour.xes.`in`.XesXmlParser
import java.io.File
import java.io.InputStream

fun interface EpaBuildProgressCallback {
    fun onProgress(current: Long, total: Long, task: String)
}

class ProgressInputStream(
    private val inputStream: InputStream,
    private val totalSize: Long,
    private val progressCallback: (bytesRead: Long, totalSize: Long, percentage: Float) -> Unit,
    private val updateIntervalMs: Long = 100
) : InputStream() {

    private var bytesRead: Long = 0
    private var lastUpdateTime: Long = 0

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val result = inputStream.read(buffer, offset, length)
        if (result > 0) {
            bytesRead += result

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= updateIntervalMs) {
                notifyProgress()
                lastUpdateTime = currentTime
            }
        }
        return result
    }

    private fun notifyProgress() {
        val percentage = if (totalSize > 0) (bytesRead.toFloat() / totalSize.toFloat()) * 100f else 0f
        progressCallback(bytesRead, totalSize, percentage)
    }

    override fun read(): Int {
        val byte = inputStream.read()
        if (byte != -1) {
            bytesRead++
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= updateIntervalMs) {
                notifyProgress()
                lastUpdateTime = currentTime
            }
        }
        return byte
    }

    override fun close() {
        // Send final progress update
        notifyProgress()
        inputStream.close()
    }

    // Delegate other methods...
    override fun available() = inputStream.available()
    override fun mark(readlimit: Int) = inputStream.mark(readlimit)
    override fun reset() = inputStream.reset()
    override fun markSupported() = inputStream.markSupported()
}

/**
 * Builder class for constructing an [ExtendedPrefixAutomaton] from an XES event log file.
 *
 * This builder takes care of parsing the XES file, mapping it to domain-specific [Event]s using a provided
 * [EventLogMapper], and building the extended prefix automaton.
 *
 * @param T The timestamp type used in the event log, which must be [Comparable].
 */
class ExtendedPrefixAutomatonBuilder<T : Comparable<T>> {
    private var eventLogMapper: EventLogMapper<T>? = null
    private var file: File? = null
    private var progressCallback: EpaBuildProgressCallback? = null
    private val parser: XesXmlParser = EPAXesParser()

    private var nextPartition = 1

    /**
     * Sets the [EventLogMapper] used to convert XES [XEvent]s into domain-specific [Event]s.
     *
     * @param mapper The implementation of [EventLogMapper] to be used.
     * @return This builder instance for chaining.
     */
    fun setEventLogMapper(mapper: EventLogMapper<T>): ExtendedPrefixAutomatonBuilder<T> {
        eventLogMapper = mapper
        return this
    }

    fun setProgressCallback(callback: EpaBuildProgressCallback): ExtendedPrefixAutomatonBuilder<T> {
        progressCallback = callback
        return this
    }

    /**
     * Sets the input XES log file to be parsed and used for building the automaton.
     *
     * @param f The input file in XES format.
     * @return This builder instance for chaining.
     */
    fun setFile(f: File): ExtendedPrefixAutomatonBuilder<T> {
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
