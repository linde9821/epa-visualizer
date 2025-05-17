package moritz.lindner.masterarbeit.epa.visitor

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State

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
        csvWriter().open(path) {
            writeRow("state name", "frequency", "total events")

            writeRow("all", 1.0f, totalEventsOfActivity.values.sum())

            frequencyByState.entries.sortedByDescending { it.value }.forEach { (state, frequency) ->
                val stateName =
                    when (state) {
                        is State.PrefixState -> "${state.name} from ${state.from} via ${state.via}"
                        State.Root -> "root"
                    }
                writeRow(stateName, frequency, eventsByState[state])
            }
        }
    }
}
