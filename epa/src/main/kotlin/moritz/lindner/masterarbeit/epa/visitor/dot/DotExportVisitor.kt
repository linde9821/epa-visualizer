package moritz.lindner.masterarbeit.epa.visitor.dot

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

/**
 * A visitor that exports an [ExtendedPrefixAutomata] to the DOT graph description language,
 * which can be rendered using tools like Graphviz.
 *
 * The graph includes:
 * - Nodes labeled by event sequences per state
 * - Directed edges labeled with activity names
 * - Subgraphs representing partitions
 *
 * @param T The timestamp type used in the automaton's events.
 */
class DotExportVisitor<T : Comparable<T>> : AutomataVisitor<T> {
    private val labelByState = mutableMapOf<State, String>()
    private val transitions = mutableListOf<String>()
    private val statesByPartition = mutableMapOf<Int, MutableSet<State>>()

    /**
     * The final DOT string representation of the automaton.
     * This is initialized when [onEnd] is called after traversal.
     */
    lateinit var dot: String
        private set

    override fun onEnd(extendedPrefixAutomata: ExtendedPrefixAutomata<T>) {
        dot =
            buildString {
                appendLine("digraph EPA {")
                appendLine("    rankdir=LR;")
                appendLine("    // states (nodes)")

                labelByState
                    .forEach { (state, label) -> appendLine("    \"${state.hashCode()}\" [label=\"$label\"];") }

                appendLine("    // transitions")
                transitions.forEach { appendLine("    $it") }

                appendLine("    // partitions")
                statesByPartition.forEach { (partition, states) ->
                    appendLine("    subgraph cluster_partition$partition {")
                    appendLine("        label = \"Partition $partition\";")
                    appendLine("        color=black;")
                    states.forEach { appendLine("        \"${it.hashCode()}\"") }
                    appendLine("    }")
                }

                appendLine("}")
            }
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {
        val label =
            when (state) {
                is State.Root -> "Root"
                is State.PrefixState ->
                    extendedPrefixAutomata
                        .sequence(state)
                        .joinToString("\\n") { "${it.activity.name} ${it.caseIdentifier}" }
            }
        labelByState[state] = label

        val partition = extendedPrefixAutomata.partition(state)
        statesByPartition.getOrPut(partition) { mutableSetOf() }.add(state)
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        transition: Transition,
        depth: Int,
    ) {
        transitions.add("\"${transition.start.hashCode()}\" -> \"${transition.end.hashCode()}\" [label=\"${transition.activity.name}\"];")
    }
}
