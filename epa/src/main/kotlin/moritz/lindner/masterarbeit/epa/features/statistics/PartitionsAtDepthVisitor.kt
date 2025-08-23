package moritz.lindner.masterarbeit.epa.features.statistics

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class PartitionsAtDepthVisitor<T : Comparable<T>> : AutomatonVisitor<T> {
    val statesByDepth = mutableMapOf<Int, List<State>>()
    val paritionsByDepth = mutableMapOf<Int, Int>()

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        statesByDepth.forEach { (depth, stateList) ->
            val distinctPartions =
                stateList
                    .map { state ->
                        extendedPrefixAutomaton.partition(state)
                    }.distinct()
                    .size

            paritionsByDepth[depth] = distinctPartions
        }
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int,
    ) {
        if (statesByDepth[depth] == null) {
            statesByDepth[depth] = listOf(state)
        } else {
            statesByDepth[depth] = statesByDepth[depth]!! + state
        }
    }

    fun report(path: String) {
        csvWriter().open(path) {
            writeRow("depth", "states", "paritions")

            statesByDepth.values.zip(paritionsByDepth.values).forEachIndexed { depth, (states, paritions) ->
                writeRow(depth, states.size, paritions)
            }
        }
    }
}
