package moritz.lindner.masterarbeit.epa.builder

import io.github.oshai.kotlinlogging.KotlinLogging
import me.tongfei.progressbar.ConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.parser.EPAXesParser
import org.deckfour.xes.`in`.XesXmlParser
import java.io.File

class ExtendedPrefixAutomataBuilder<T : Comparable<T>> {
    private var eventLogMapper: EventLogMapper<T>? = null
    private var file: File? = null
    private val parser: XesXmlParser = EPAXesParser()

    private var nextPartition = 1

    private val logger = KotlinLogging.logger { }

    inner class StateActivityKey(
        val state: State,
        val activity: Activity,
    )

    fun setEventLogMapper(mapper: EventLogMapper<T>): ExtendedPrefixAutomataBuilder<T> {
        eventLogMapper = mapper
        return this
    }

    fun setFile(f: File): ExtendedPrefixAutomataBuilder<T> {
        file = f
        return this
    }

    fun build(): ExtendedPrefixAutomata<T> {
        require(eventLogMapper != null) { "plainEventLog cannot be null" }
        require(file != null) { "file cannot be null" }
        require(file!!.exists()) { "file must exist" }
        require(parser.canParse(file!!)) { "file can't be parsed" }

        val log = parser.parse(file!!.inputStream()).first()

        val plainEventLog =
            eventLogMapper!!.build(
                ProgressBar.wrap(
                    log!!,
                    ProgressBarBuilder()
                        .showSpeed()
                        .setConsumer(ConsoleProgressBarConsumer(System.out))
                        .setTaskName("plain log")
                        .setMaxRenderedLength(80)
                        .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                        .setUnit(" Traces", 1L),
                ),
            )

        val plainLog =
            ProgressBar.wrap(
                plainEventLog,
                ProgressBarBuilder()
                    .showSpeed()
                    .setConsumer(ConsoleProgressBarConsumer(System.out))
                    .setTaskName("EPA")
                    .setMaxRenderedLength(80)
                    .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                    .setUnit(" Events", 1L),
            )

        val states = hashSetOf<State>(State.Root)
        val transitions = hashSetOf<Transition>()
        val activities = hashSetOf<Activity>()
        val sequences = hashMapOf<State, HashSet<Event<T>>>(State.Root to hashSetOf())
        val partitionByState = hashMapOf<State, Int>(State.Root to 0)
        val lastActivityByState = hashMapOf<String, State>()
        val transitionByPredecessorStateAndActivity = hashMapOf<StateActivityKey, Transition>()
        val transitionByPredecessorState = hashMapOf<State, Transition>()

        plainLog.forEach { event ->
            val predecessorState = lastActivityByState[event.caseIdentifier] ?: State.Root

            val existingTransition = transitionByPredecessorStateAndActivity[StateActivityKey(predecessorState, event.activity)]

            val currentActivity =
                if (existingTransition != null) {
                    existingTransition.end
                } else {
                    val existingTransitionFromPredecessor = transitionByPredecessorState[predecessorState]

                    val c = getPartition(existingTransitionFromPredecessor, partitionByState, predecessorState)

                    val newState =
                        State.PrefixState(
                            from = predecessorState,
                            via = event.activity,
                        )
                    states.add(newState)
                    val newTransition = Transition(predecessorState, event.activity, newState)
                    transitionByPredecessorStateAndActivity[StateActivityKey(predecessorState, event.activity)] = newTransition
                    transitionByPredecessorState[predecessorState] = newTransition
                    transitions.add(newTransition)
                    activities.add(event.activity)
                    partitionByState[newState] = c
                    newState
                }

            sequences.computeIfAbsent(currentActivity) { HashSet() }.add(event)
            lastActivityByState[event.caseIdentifier] = currentActivity
        }

        return ExtendedPrefixAutomata(
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
