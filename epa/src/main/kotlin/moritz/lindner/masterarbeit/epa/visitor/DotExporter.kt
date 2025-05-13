package moritz.lindner.masterarbeit.epa.visitor

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

class DotExporter<T : Comparable<T>>(
    private val epa: ExtendedPrefixAutomata<T>,
) : AutomataVisitor<T> {
    private val labelByState = mutableMapOf<State, String>()
    private val transitions = mutableListOf<String>()
    private val statesByPartition = mutableMapOf<Int, MutableSet<State>>()

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {
        val label =
            when (state) {
                is State.Root -> "Root"
                is State.PrefixState ->
                    epa
                        .sequence(state)
                        .joinToString("\\n") { "${it.activity.name} ${it.caseIdentifier}" }
            }
        labelByState[state] = label

        val partition = epa.partition(state)
        statesByPartition.getOrPut(partition) { mutableSetOf() }.add(state)
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        transition: Transition,
        depth: Int,
    ) {
        transitions.add("\"${transition.start.hashCode()}\" -> \"${transition.end.hashCode()}\" [label=\"${transition.activity.name}\"];")
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        event: Event<T>,
        depth: Int,
    ) {
        // nothing todo
    }

    fun buildDot(): String {
        val sb = StringBuilder()
        sb.appendLine("digraph EPA {")
        sb.appendLine("    rankdir=LR;")
        sb.appendLine("    // states (nodes)")

        labelByState.forEach { (state, label) -> sb.appendLine("    \"${state.hashCode()}\" [label=\"$label\"];") }

        sb.appendLine("    // transitions")
        transitions.forEach { sb.appendLine("    $it") }

        sb.appendLine("    // partitions")
        statesByPartition.forEach { (partition, states) ->
            sb.appendLine("    subgraph cluster_partition$partition {")
            sb.appendLine("        label = \"Partition $partition\";")
            sb.appendLine("        color=black;")
            states.forEach { sb.appendLine("        \"${it.hashCode()}\"") }
            sb.appendLine("    }")
        }

        sb.appendLine("}")
        return sb.toString()
    }
}
