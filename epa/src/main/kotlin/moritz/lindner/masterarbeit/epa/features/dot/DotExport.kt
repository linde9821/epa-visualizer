package moritz.lindner.masterarbeit.epa.features.dot

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

/**
 * A visitor that exports an [ExtendedPrefixAutomaton] to the DOT graph
 * description language, which can be rendered using tools like Graphviz.
 *
 * The graph includes:
 * - Nodes labeled by event sequences per state
 * - Directed edges labeled with activity names
 * - Subgraphs representing partitions
 *
 * @param T The timestamp type used in the automaton's events.
 */
class DotExport<T : Comparable<T>> : AutomatonVisitor<T> {
    private val labelByState = mutableMapOf<State, String>()
    private val transitions = mutableListOf<String>()
    private val statesByPartition = mutableMapOf<Int, MutableSet<State>>()

    /**
     * The final DOT string representation of the automaton. This is
     * initialized when [onEnd] is called after traversal.
     */
    lateinit var dot: String
        private set

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        dot =
            buildString {
                appendLine("digraph EPA {")
                appendLine("    nodesep=1;")
                appendLine("    ranksep=.4;")
                appendLine("    rankdir=LR;")
                appendLine("    graph [fontname=\"Times New Roman\"];")
                appendLine("    node [fontname=\"Times New Roman\", fontsize=15, shape=circle, fixedsize=true, width=0.8];")
                appendLine("    edge [fontname=\"Times New Roman\", fontsize=18];")
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
                    appendLine("        fontname = \"Times-Roman\";")
                    appendLine("        fontsize = 18")
                    states.forEach { appendLine("        \"${it.hashCode()}\"") }
                    appendLine("    }")
                }

                appendLine("}")
            }
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int,
    ) {
        val label =
            when (state) {
                is State.Root -> "root"
                is State.PrefixState -> {
                    val sequence =
                        extendedPrefixAutomaton
                            .sequence(state)
                    val length = sequence.size
                    sequence
                        .mapIndexed { index, event ->
                            if (index % 2 == 0 && index + 1 != length) {
                                "${event.activity.name}${event.caseIdentifier}, "
                            } else {
                                "${event.activity.name}${event.caseIdentifier}\\n"
                            }
                        }.joinToString("")
                }
            }
        labelByState[state] = label

        val partition = extendedPrefixAutomaton.partition(state)
        statesByPartition.getOrPut(partition) { mutableSetOf() }.add(state)
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        transition: Transition,
        depth: Int,
    ) {
        transitions.add("\"${transition.start.hashCode()}\" -> \"${transition.end.hashCode()}\" [label=\"${transition.activity.name}\"];")
    }
}
