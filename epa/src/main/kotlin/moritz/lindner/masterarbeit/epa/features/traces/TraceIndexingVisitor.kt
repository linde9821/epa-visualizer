package moritz.lindner.masterarbeit.epa.features.traces

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor


class TraceIndexingVisitor<T>(
    private val progressCallback: EpaProgressCallback? = null
) : AutomatonVisitor<T> where T : Comparable<T> {

    private val allEvents = mutableListOf<Event<T>>()

    private lateinit var traceByCaseId: Map<String, List<Event<T>>>

    fun getTraceByCaseIdentifierSortedByTimestamp(caseIdentifier: String): List<Event<T>> {
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

    override fun onProgress(current: Long, total: Long) {
        progressCallback?.onProgress(current, total, "Indexing Traces")
    }
}