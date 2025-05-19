package moritz.lindner.masterarbeit.epa.visitor.dot

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

class DotExportVisitor<T : Comparable<T>> : AutomataVisitor<T> {
    private val labelByState = mutableMapOf<State, String>()
    private val transitions = mutableListOf<String>()
    private val statesByPartition = mutableMapOf<Int, MutableSet<State>>()

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
