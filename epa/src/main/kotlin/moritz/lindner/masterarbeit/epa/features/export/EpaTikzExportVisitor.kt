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
            appendLine(
                """
                % IMPORTANT: Compile with LuaLaTeX
                % add to preamble
                \usepackage{tikz}
                \usetikzlibrary{
                    shapes.geometric, 
                    positioning, 
                    graphs,
                    graphdrawing,
                    arrows.meta,
                    fit,
                    backgrounds,
                    quotes,
                    babel,
                    matrix
                }
                \usegdlibrary{trees} % Or 'layered'
                %%%%%%%%%%%%%%
            """.trimIndent()
            )
            appendLine("")

            appendLine(
                """
        \begin{tikzpicture}
            \graph [
                tree layout,
                nodes={
                    draw,
                    grow=right,
                    rounded corners,
                    font=\scriptsize,
                    fill=white,
                    align=center
                },
                edge quotes={
                    font=\scriptsize,
                    inner sep=2pt,
                    auto,
                    sloped,
                    pos=0.5
                },
                sibling distance=3.5cm,
                layer distance=4.3cm
            ] {
            """.trimIndent()
            )
            // 1. Define nodes and their labels
            val allStates = statesByPartition.values.flatten()
            allStates.forEachIndexed { index, state ->
                val id = if (state is State.Root) "root" else "s${state.hashCode().toString().replace("-", "n")}"
                val label = getTikzLabel(extendedPrefixAutomaton, state, index)
                appendLine("        $id [as={$label}];")
            }

            appendLine("")
            // 2. Define edges (transitions)
            transitions.forEach { appendLine("    $it") }
            appendLine("  };")

            // 3. Partitions (Irregular Hulls)
            appendLine(
                """
            \begin{scope}[on background layer,
                hull/.style={
                    line width=2.8cm, 
                    line cap=round,
                    line join=round,
                    opacity=0.4
                }]
        """.trimIndent()
            )

            val colors = listOf("magenta", "cyan", "red", "green", "lime")
            val legendEntries = mutableListOf<String>()

            statesByPartition.entries.forEachIndexed { index, (c, statesSet) ->
                if (statesSet.isEmpty()) return@forEachIndexed
                val states = statesSet.toList() // Convert to list to access middle element
                val color = colors[index % colors.size]

                val partitionLabel = if (c == 0) {
                    "$\\bot$"
                } else c.toString()
                legendEntries.add("""\fill[$color, opacity=0.4] (0,0) rectangle (0.4,0.2); \# \node[anchor=west, font=\scriptsize]{Partition $partitionLabel};""")

                if (states.size > 1) {
                    val path = states.joinToString(" -- ") {
                        "(${if (it is State.Root) $$"$root$" else "s${it.hashCode().toString().replace("-", "n")}"}.center)"
                    }
                    appendLine("        \\draw[hull, $color] $path;")
                } else {
                    val singleId = if (states.first() is State.Root) "root" else "s${
                        states.first().hashCode().toString().replace("-", "n")
                    }"
                    appendLine("        \\node[draw=$color!50, fill=$color!10, rounded corners, fit=($singleId)] {};")
                }
            }
            appendLine("    \\end{scope}")

            // 4. The Legend
            if (legendEntries.isNotEmpty()) {
                appendLine(
                    """
                                    \matrix [
                                        draw,
                                        fill=white,
                                        rounded corners,
                                        at=(current bounding box.north west),
                                        anchor=north west,
                                        xshift=0.5cm,
                                        yshift=-0.5cm,
                                        column sep=5pt,
                                        ampersand replacement=\#
                                    ] {
                                        ${legendEntries.joinToString(" \\\\ \n                ")} \\
                                    };
    """.trimIndent()
                )
            }

            appendLine("\\end{tikzpicture}")
        }
    }

    private fun getTikzLabel(epa: ExtendedPrefixAutomaton<T>, state: State, index: Int): String {
        return when (state) {
            is State.Root -> $$"$root$"
            is State.PrefixState -> {
                val sequence = epa.sequence(state).joinToString(",\\\\") {
                    "\$${it.activity.name}_{${it.caseIdentifier}}\$"
                }
                $$"$s_$$index \\{$ $$sequence $\\}$"
            }
        }
    }

    override fun visit(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>, state: State, depth: Int) {
        statesByPartition.getOrPut(extendedPrefixAutomaton.partition(state)) { mutableSetOf() }.add(state)
    }

    override fun visit(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>, transition: Transition, depth: Int) {
        val s = if (transition.start is State.Root) "root" else "s${
            transition.start.hashCode().toString().replace("-", "n")
        }"
        val e =
            if (transition.end is State.Root) "root" else "s${transition.end.hashCode().toString().replace("-", "n")}"
        val act = transition.activity.name
        transitions.add("    $s ->[\"$act\"] $e;")
    }
}