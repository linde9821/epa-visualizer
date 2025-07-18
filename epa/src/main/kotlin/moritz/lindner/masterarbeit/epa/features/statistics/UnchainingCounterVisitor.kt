package moritz.lindner.masterarbeit.epa.features.statistics

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class UnchainingCounterVisitor<T : Comparable<T>> : AutomatonVisitor<T> {
    private val transitions = LinkedHashMap<State, List<State>>()
    private val chainedTransitions = LinkedHashMap<List<State>, List<State>>()

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        val stack: ArrayDeque<State> = ArrayDeque()

        transitions.forEach { (state, outgoing) ->
            stack.addFirst(state)
            if (
                outgoing.size > 1 ||
                outgoing.isEmpty() ||
                !transitions.containsKey(outgoing.first())
            ) { // start building chain
                val chain = stack.toList().reversed()
                stack.clear()
                chainedTransitions[chain] = outgoing
            }
        }
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        transition: Transition,
        depth: Int,
    ) {
        transitions.merge(transition.start, listOf(transition.end)) { a, b -> a + b }
    }

    fun report(path: String) {
        val unchainedSize = transitions.size
        val chainedSize = chainedTransitions.size
        csvWriter().open(path) {
            writeRow("state", "children")

            writeRow("unchained size", unchainedSize)

            transitions.forEach { (state, outgoing) ->
                writeRow(state, outgoing.joinToString(separator = " | "))
            }
            writeRow("", "")

            writeRow("chained size", chainedSize)

            chainedTransitions.forEach { (state, outgoing) ->
                writeRow(state.joinToString(" | "), outgoing.joinToString(separator = " | "))
            }
        }
    }
}
