package moritz.lindner.masterarbeit.epa.visitor.statistics

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor
import kotlin.collections.component1
import kotlin.collections.component2

class PartitionsAtDepthVisitor<T : Comparable<T>> : AutomataVisitor<T> {
    val statesByDepth = mutableMapOf<Int, List<State>>()
    val paritionsByDepth = mutableMapOf<Int, Int>()

    override fun onEnd(extendedPrefixAutomata: ExtendedPrefixAutomata<T>) {
        statesByDepth.forEach { (depth, stateList) ->
            val distinctPartions =
                stateList
                    .map { state ->
                        extendedPrefixAutomata.partition(state)
                    }.distinct()
                    .size

            paritionsByDepth[depth] = distinctPartions
        }
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
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
