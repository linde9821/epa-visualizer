package moritz.lindner.masterarbeit.epa

import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

/**
 * Not thread safe to visit
 */
data class ExtendedPrefixAutomata<T : Comparable<T>>(
    val states: Set<State>,
    val activities: Set<Activity>,
    val transitions: Set<Transition>,
    private val partitionByState: Map<State, Int>,
    private val sequenceByState: Map<State, Set<Event<T>>>,
) {
    // for improved visitor speed
    private val outgoingTransitionsByState = transitions.groupBy { it.start }

    fun partition(start: State): Int = partitionByState[start] ?: throw IllegalStateException("No state with start $start")

    fun sequence(state: State): Set<Event<T>> = sequenceByState[state] ?: throw IllegalStateException("No sequence for state $state")

    private var visitedStates = 0L

    fun acceptDepthFirst(visitor: AutomataVisitor<T>) {
        visitedStates = 0
        visitDepthFirst(visitor, State.Root)
    }

    fun acceptBreadthFirst(visitor: AutomataVisitor<T>) {
        visitedStates = 0
        visitBreadthFirst(visitor)
    }

    private fun visitBreadthFirst(visitor: AutomataVisitor<T>) {
        data class Inner(
            val state: State,
            val depth: Int,
        )

        val deque = ArrayDeque<Inner>(states.size / 2)
        deque.add(Inner(State.Root, 0))

        while (deque.isNotEmpty()) {
            visitedStates++
            val (state, depth) = deque.removeFirst()
            visitor.onProgress(visitedStates, states.size.toLong())

            visitor.visit(state, depth)

            val seq = sequence(state)
            val transitions = outgoingTransitionsByState[state] ?: emptyList()

            seq.forEach { event ->
                visitor.visit(event, depth)
            }

            transitions.forEach { transition ->
                visitor.visit(transition, depth)
                deque.addLast(Inner(transition.end, depth + 1))
            }
        }
    }

    private fun visitDepthFirst(
        visitor: AutomataVisitor<T>,
        state: State,
        depth: Int = 0,
    ) {
        visitor.visit(state, depth)
        visitedStates++
        visitor.onProgress(visitedStates, states.size.toLong())

        sequence(state).forEach { event ->
            visitor.visit(event, depth)
        }

        outgoingTransitionsByState[state]
            ?.onEach { transition ->
                visitor.visit(transition, depth)
            }?.forEach { transition ->
                visitDepthFirst(
                    visitor = visitor,
                    state = transition.end,
                    depth = depth + 1,
                )
            }
    }
}
