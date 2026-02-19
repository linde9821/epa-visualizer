package moritz.lindner.masterarbeit.epa.features.statistics

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class StatesAndPartitionsByDepthVisitor<T : Comparable<T>> : AutomatonVisitor<T> {
    val statesByDepth = mutableMapOf<Int, List<State>>()
    val partitionsByDepth = mutableMapOf<Int, Int>()
    lateinit var epa: ExtendedPrefixAutomaton<T>

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        epa = extendedPrefixAutomaton
        statesByDepth.forEach { (depth, stateList) ->
            val distinctPartitionsAtDepth = stateList
                .map { state -> extendedPrefixAutomaton.partition(state) }
                .distinct()
                .size

            partitionsByDepth[depth] = distinctPartitionsAtDepth
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
            writeRow("depth", "states", "partitions", "events-count")

            statesByDepth.values.zip(partitionsByDepth.values).forEachIndexed { depth, (states, partitions) ->
                val events = states.sumOf { epa.sequence(it).size }
                writeRow(depth, states.size, partitions, events)
            }
        }
    }
}
