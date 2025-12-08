package moritz.lindner.masterarbeit.epa.features.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.traces.TraceIndexingVisitor
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

/**
 * Computes the normalized frequency of traces per Partition in an
 * [moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton].
 *
 * The root partition (0)  is always assigned a frequency of 1.0.
 *
 * The other partitions are calculated by calculating the amount of traces ending in a given partition.
 * As all states are accepting states it doesn't matter where the state is in relation to the partition.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class NormalizedPartitionFrequencyVisitor<T>(
    traceIndexingVisitor: TraceIndexingVisitor<T>,
    private val progressCallback: EpaProgressCallback? = null
) : AutomatonVisitor<T> where T : Comparable<T> {

    private val countOfTracesEndingInPartition = HashMap<Int, Int>()
    private val lastEvents = traceIndexingVisitor.getAllTraces().map { caseIdentifier ->
        traceIndexingVisitor.getTraceByCaseIdentifierSortedByTimestamp(caseIdentifier).last()
    }.toSet()

    private lateinit var relativeFrequencyByPartition: Map<Int, Float>

    fun build(): NormalizedPartitionFrequency {
        return NormalizedPartitionFrequency(relativeFrequencyByPartition)
    }

    override fun onStart(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        extendedPrefixAutomaton.getAllPartitions().forEach { c ->
            countOfTracesEndingInPartition[c] = 0
        }
    }

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        val totalTraces = lastEvents.size.toFloat()

        relativeFrequencyByPartition = buildMap {
            countOfTracesEndingInPartition.forEach { (c, count) ->
                // we always need the partition of Root
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

    override fun onProgress(current: Long, total: Long) {
        progressCallback?.onProgress(current, total, "Relative partition frequency")
    }
}