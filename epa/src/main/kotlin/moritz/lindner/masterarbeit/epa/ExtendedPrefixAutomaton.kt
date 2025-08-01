package moritz.lindner.masterarbeit.epa

import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

/**
 * A non-mutable, non-thread-safe data structure representing an Extended Prefix Automaton (EPA).
 *
 * The automaton is built from a set of [State]s, [Activity] labels, and [Transition] edges,
 * along with mappings that associate states with partitions and sequences of events.
 *
 * It supports both depth-first and breadth-first traversal via a [AutomatonVisitor], and maintains
 * efficient access to outgoing transitions for fast traversal.
 *
 * **Note:** This class is not thread-safe for concurrent visiting or mutation. When multiple threads might
 * access the ExtendedPrefixAutomaton create a new instance of the EPA with the `copy` function and let it
 * accept the visitor.
 *
 * @param T The timestamp type used in the associated events.
 * @property states All states in the automaton, including the root.
 * @property activities All distinct activities appearing in transitions.
 * @property transitions All transitions between states, each labeled with an activity.
 */
class ExtendedPrefixAutomaton<T : Comparable<T>>(
    val eventLogName: String,
    val states: Set<State>,
    val activities: Set<Activity>,
    val transitions: Set<Transition>,
    private val partitionByState: Map<State, Int>,
    private val sequenceByState: Map<State, Set<Event<T>>>,
) {
    // Cached grouping of transitions by their source state
    private val outgoingTransitionsByState by lazy { transitions.groupBy { it.start } }

    /**
     * Returns the partition index assigned to the given state.
     *
     * @throws IllegalStateException If the state is not part of the automaton.
     */
    fun partition(start: State): Int = partitionByState[start] ?: throw IllegalStateException("No state with start $start")

    /**
     * Returns all events associated with the given state.
     *
     * @throws IllegalStateException If the state is not part of the automaton.
     */
    fun sequence(state: State): Set<Event<T>> = sequenceByState[state] ?: throw IllegalStateException("No sequence for state $state")

    private var visitedStates = 0L

    /**
     * Returns a list of all unique partition indices present in the automaton.
     */
    fun getAllPartitions(): List<Int> = partitionByState.values.distinct().toList()

    /**
     * Traverses the automaton in depth-first order using the provided [AutomatonVisitor].
     *
     * The visitor will be invoked in the following order for each state:
     * 1. [State]
     * 2. Events of the state
     * 3. Transitions from the state
     *
     * Starts at the [State.Root].
     */
    fun acceptDepthFirst(visitor: AutomatonVisitor<T>) {
        visitedStates = 0
        visitor.onStart(this)
        visitDepthFirst(visitor, State.Root)
        visitor.onEnd(this)
    }

    /**
     * Traverses the automaton in breadth-first order using the provided [AutomatonVisitor].
     *
     * The visitor will be invoked in the following order for each state:
     * 1. [State]
     * 2. Events of the state
     * 3. Transitions from the state
     */
    fun acceptBreadthFirst(visitor: AutomatonVisitor<T>) {
        visitedStates = 0
        visitor.onStart(this)
        visitBreadthFirst(visitor)
        visitor.onEnd(this)
    }

    private fun visitBreadthFirst(visitor: AutomatonVisitor<T>) {
        data class StateAndDepth(
            val state: State,
            val depth: Int,
        )

        val deque = ArrayDeque<StateAndDepth>(states.size / 2)
        deque.add(StateAndDepth(State.Root, 0))

        while (deque.isNotEmpty()) {
            visitedStates++
            val (state, depth) = deque.removeFirst()
            visitor.onProgress(visitedStates, states.size.toLong())

            visitor.visit(this, state, depth)

            val seq = sequence(state)
            val transitions = outgoingTransitionsByState[state] ?: emptyList()

            seq.forEach { event ->
                visitor.visit(this, event, depth)
            }

            transitions.forEach { transition ->
                visitor.visit(this, transition, depth)
                deque.addLast(StateAndDepth(transition.end, depth + 1))
            }
        }
    }

    private fun visitDepthFirst(
        visitor: AutomatonVisitor<T>,
        state: State,
        depth: Int = 0,
    ) {
        visitor.visit(this, state, depth)
        visitedStates++
        visitor.onProgress(visitedStates, states.size.toLong())

        sequence(state).forEach { event ->
            visitor.visit(this, event, depth)
        }

        outgoingTransitionsByState[state]
            ?.onEach { transition ->
                visitor.visit(this, transition, depth)
            }?.forEach { transition ->
                visitDepthFirst(
                    visitor = visitor,
                    state = transition.end,
                    depth = depth + 1,
                )
            }
    }

    override fun toString(): String =
        buildString {
            appendLine(eventLogName)
            appendLine(states.joinToString(","))
            appendLine(activities.joinToString(","))
            appendLine(transitions.joinToString(","))
            appendLine(partitionByState.map { "${it.key}:${it.value}" }.joinToString(","))
            appendLine(sequenceByState.map { "${it.key}:${it.value.joinToString(",")}" }.joinToString(","))
        }

    /**
     * Returns a deep copy of this automaton.
     */
    fun copy(): ExtendedPrefixAutomaton<T> =
        ExtendedPrefixAutomaton(
            eventLogName = eventLogName,
            states = states.toSet(),
            activities = activities.toSet(),
            transitions = transitions.map { it.copy() }.toSet(),
            partitionByState = partitionByState.toMap(),
            sequenceByState = sequenceByState.mapValues { it.value.toSet() },
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExtendedPrefixAutomaton<*>

        if (eventLogName != other.eventLogName) return false
        if (states != other.states) return false
        if (activities != other.activities) return false
        if (transitions != other.transitions) return false
        if (partitionByState != other.partitionByState) return false
        if (sequenceByState != other.sequenceByState) return false
        if (outgoingTransitionsByState != other.outgoingTransitionsByState) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eventLogName.hashCode()
        result = 31 * result + states.hashCode()
        result = 31 * result + activities.hashCode()
        result = 31 * result + transitions.hashCode()
        result = 31 * result + partitionByState.hashCode()
        result = 31 * result + sequenceByState.hashCode()
        result = 31 * result + outgoingTransitionsByState.hashCode()
        return result
    }
}
