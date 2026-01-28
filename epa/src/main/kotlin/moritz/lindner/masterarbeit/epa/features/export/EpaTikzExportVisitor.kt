package moritz.lindner.masterarbeit.epa.features.export

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class EpaTikzExporter<T : Comparable<T>> : AutomatonVisitor<T> {
    private val transitions = mutableListOf<String>()
    private val statesByPartition = mutableMapOf<Int, MutableSet<State>>()

    lateinit var tikz: String
        private set

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        tikz = buildString {
            appendLine("% IMPORTANT: Compile with LuaLaTeX")
            appendLine("\\usepackage{tikz}")
            appendLine("\\usetikzlibrary{graphs, graphdrawing, arrows.meta, fit, backgrounds}")
            appendLine("\\usegdlibrary{layered} % Or 'trees'")
            appendLine("")
            appendLine("\\begin{tikzpicture}")
            appendLine("  \\graph [layered layout, nodes={draw, rounded corners, font=\\scriptsize, fill=white, align=center}, ")
            appendLine("          edge quotes={font=\\tiny, inner sep=2pt}, sibling distance=2cm, layer distance=3cm] {")

            // 1. Define nodes and their labels
            val allStates = statesByPartition.values.flatten()
            allStates.forEach { state ->
                val id = if (state is State.Root) "root" else "s${state.hashCode().toString().replace("-", "n")}"
                val label = getTikzLabel(extendedPrefixAutomaton, state)
                appendLine("    $id [as={$label}]" + (if(state is State.Root) ", circle" else "") + ";")
            }

            appendLine("")
            // 2. Define edges (transitions)
            transitions.forEach { appendLine("    $it") }
            appendLine("  };")

            // 3. Partitions (Background Fit)
            appendLine("  \\begin{scope}[on background layer]")
            statesByPartition.forEach { (pIdx, states) ->
                val group = states.joinToString(" ") {
                    "(${if (it is State.Root) "root" else "s${it.hashCode().toString().replace("-", "n")}"})"
                }
                appendLine("    \\node[draw=gray, dashed, inner sep=0.5cm, fit=$group, label=above:Partition $pIdx] {};")
            }
            appendLine("  \\end{scope}")
            appendLine("\\end{tikzpicture}")
        }
    }

    private fun getTikzLabel(epa: ExtendedPrefixAutomaton<T>, state: State): String {
        return when (state) {
            is State.Root -> "root"
            is State.PrefixState -> epa.sequence(state).joinToString("\\\\") {
                "\$${it.activity.name}_{${it.caseIdentifier}}\$"
            }
        }
    }

    override fun visit(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>, state: State, depth: Int) {
        statesByPartition.getOrPut(extendedPrefixAutomaton.partition(state)) { mutableSetOf() }.add(state)
    }

    override fun visit(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>, transition: Transition, depth: Int) {
        val s = if (transition.start is State.Root) "root" else "s${transition.start.hashCode().toString().replace("-", "n")}"
        val e = if (transition.end is State.Root) "root" else "s${transition.end.hashCode().toString().replace("-", "n")}"
        val act = transition.activity.name
        transitions.add("    $s ->[\"$act\"] $e;")
    }
}