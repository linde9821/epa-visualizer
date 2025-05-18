package moritz.lindner.masterarbeit.epa.visitor.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

class BranchFrequencyVisitor<T : Comparable<T>> : AutomataVisitor<T> {
    private val eventsByState = HashMap<State, Int>()
    private val frequencyByState = HashMap<State, Float>()
    private var totalEventsOfActivity = HashMap<Activity, Int>()

    override fun onEnd(extendedPrefixAutomata: ExtendedPrefixAutomata<T>) {
        eventsByState.forEach { (state, eventsSeen) ->
            when (state) {
                is State.PrefixState -> {
                    frequencyByState[state] = eventsSeen.toFloat() / totalEventsOfActivity[state.via]!!.toFloat()
                }

                State.Root -> frequencyByState[state] = 0f
            }
        }
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {
        eventsByState.computeIfAbsent(state) {
            extendedPrefixAutomata.sequence(state).size
        }
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        event: Event<T>,
        depth: Int,
    ) {
        totalEventsOfActivity.merge(event.activity, 1) { a, b -> a + b }
    }

    fun report(path: String) {
        val bigger1P = frequencyByState.values.filter { it > 0.01 }.count()
        val smaller1P = frequencyByState.values.filter { it <= 0.01 }.count()

        println("Nodes with more than 1% $bigger1P")
        println("Nodes with less than 1% $smaller1P")

//        csvWriter().open(path) {
//            writeRow("state name", "frequency", "total events")
//
//            writeRow("all", 1.0f, totalEventsOfActivity.values.sum())
//
//            frequencyByState.entries.sortedByDescending { it.value }.forEach { (state, frequency) ->
//                val stateName =
//                    when (state) {
//                        is State.PrefixState -> "${state.name} from ${state.from} via ${state.via}"
//                        State.Root -> "root"
//                    }
//                writeRow(stateName, frequency, eventsByState[state])
//            }
//        }
    }
}
