package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

class ChainFilter<T : Comparable<T>> :
    EpaFilter<T>,
    AutomataVisitor<T> {
    private val childrenByState = hashMapOf<State, List<State>>()

    override fun apply(epa: ExtendedPrefixAutomata<T>): ExtendedPrefixAutomata<T> {
        epa.acceptDepthFirst(this)

        val singleChildStates =
            childrenByState.filter { (_, children) ->
                children.size <= 1
            }

        return ExtendedPrefixAutomata(
            states = epa.states.filter { singleChildStates[it] == null }.toSet(),
            activities = emptySet(),
            transitions = emptySet(),
            partitionByState = emptyMap(),
            sequenceByState = emptyMap(),
        )
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {
        when (state) {
            is State.PrefixState -> {
                childrenByState[state] = childrenByState[state].orEmpty()
                childrenByState[state.from] = childrenByState[state.from].orEmpty() + state
            }
            State.Root -> {
                childrenByState[state] = listOf()
            }
        }
    }
}
