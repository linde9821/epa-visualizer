package moritz.lindner.masterarbeit.epa.features.traces

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

/**
 * Computes the normalized frequency of traces per [State] in an
 * [ExtendedPrefixAutomaton].
 *
 * TODO: add description
 *
 * The root state ([State.Root]) is always assigned
 * a frequency of 1.0. This visitor must be applied
 * via [ExtendedPrefixAutomaton.acceptDepthFirst] or
 * [acceptBreadthFirst] before accessing any frequencies.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class NormalizedPartitionFrequencyVisitorV2<T>(
    traceAccessIndex: TraceAccessIndex<T>
) : AutomatonVisitor<T> where T : Comparable<T> {

    private val countOfTracesEndingInPartition = HashMap<Int, Int>()
    private val lastEvents = traceAccessIndex.getAllTraces().map { caseIdentifier ->
        traceAccessIndex.getTraceByCaseIdentifier(caseIdentifier).last()
    }.toSet()

    private lateinit var relativeFrequencyByPartition: Map<Int, Float>

    override fun onStart(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        extendedPrefixAutomaton.getAllPartitions().forEach { c ->
            countOfTracesEndingInPartition[c] = 0
        }
    }

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        val totalTraces = lastEvents.size.toFloat()

        relativeFrequencyByPartition = buildMap {
            countOfTracesEndingInPartition.forEach { (c, count) ->
                if (c == 0) {
                    put(0, 1.0f)
                } else {
                    val p = count.toFloat() / totalTraces
                    put(c, p)
                }
            }
        }
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int
    ) {
        val seq = extendedPrefixAutomaton.sequence(state)
        val partition = extendedPrefixAutomaton.partition(state)
        lastEvents.forEach { event ->
            if (event in seq) {
                countOfTracesEndingInPartition.merge(partition, 1) { a, b -> a + b }
            }
        }
    }
}


class TraceAccessIndex<T> : AutomatonVisitor<T> where T : Comparable<T> {

    private val allEvents = mutableListOf<Event<T>>()

    private lateinit var traceByCaseId: Map<String, List<Event<T>>>

    fun getTraceByCaseIdentifier(caseIdentifier: String): List<Event<T>> {
        return traceByCaseId[caseIdentifier]!!
    }

    fun getTraceByEvent(event: Event<T>): List<Event<T>> {
        return traceByCaseId[event.caseIdentifier]!!
    }

    fun getAllTraces() = traceByCaseId.keys

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        traceByCaseId = allEvents
            .groupBy { it.caseIdentifier }
            .mapValues { (_, events) -> events.sortedBy(Event<T>::timestamp) }
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        event: Event<T>,
        depth: Int
    ) {
        allEvents.add(event)
    }
}