package moritz.lindner.masterarbeit.epa.features.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.traces.TraceAccessIndex
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

/**
 * Computes the normalized frequency of traces per
 * [moritz.lindner.masterarbeit.epa.domain.State] in an
 * [moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton].
 *
 * The root state ([moritz.lindner.masterarbeit.epa.domain.State.Root]) is
 * always assigned a frequency of 1.0. This visitor must be applied via
 * [moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton.acceptDepthFirst]
 * or [acceptBreadthFirst] before accessing any frequencies.
 *
 * @param T The timestamp type used in the automaton's events.
 *
 * TODO: add description
 */
class NormalizedPartitionFrequencyVisitorV2<T>(
    traceAccessIndex: TraceAccessIndex<T>,
    private val progressCallback: EpaProgressCallback? = null
) : AutomatonVisitor<T> where T : Comparable<T> {

    private val countOfTracesEndingInPartition = HashMap<Int, Int>()
    private val lastEvents = traceAccessIndex.getAllTraces().map { caseIdentifier ->
        traceAccessIndex.getTraceByCaseIdentifier(caseIdentifier).last()
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