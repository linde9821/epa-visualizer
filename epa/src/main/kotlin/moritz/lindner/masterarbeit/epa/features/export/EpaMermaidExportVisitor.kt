package moritz.lindner.masterarbeit.epa.features.export

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class EpaMermaidExportVisitor<T : Comparable<T>> : AutomatonVisitor<T> {
    private val transitions = mutableListOf<String>()
    private val statesByPartition = mutableMapOf<Int, MutableSet<State>>()

    lateinit var mermaid: String
        private set

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        mermaid = buildString {
            appendLine("graph LR")

            statesByPartition.forEach { (pIdx, states) ->
                appendLine("    subgraph Partition_$pIdx")
                states.forEach { state ->
                    val id = "id${state.hashCode()}".replace("-", "n")
                    val label = getMermaidLabel(extendedPrefixAutomaton, state)
                    val shape = if (state is State.Root) "(($label))" else "[$label]"
                    appendLine("        $id$shape")
                }
                appendLine("    end")
            }

            transitions.forEach { appendLine("    $it") }
        }
    }

    private fun getMermaidLabel(epa: ExtendedPrefixAutomaton<T>, state: State): String {
        return when (state) {
            is State.Root -> "root"
            is State.PrefixState -> epa.sequence(state).joinToString("<br/>") {
                "${it.activity.name}${it.caseIdentifier}"
            }
        }
    }

    override fun visit(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>, state: State, depth: Int) {
        statesByPartition.getOrPut(extendedPrefixAutomaton.partition(state)) { mutableSetOf() }.add(state)
    }

    override fun visit(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>, transition: Transition, depth: Int) {
        val s = "id${transition.start.hashCode()}".replace("-", "n")
        val e = "id${transition.end.hashCode()}".replace("-", "n")
        transitions.add("    $s -- ${transition.activity.name} --> $e")
    }
}