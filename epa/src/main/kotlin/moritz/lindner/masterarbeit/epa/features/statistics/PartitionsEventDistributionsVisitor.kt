package moritz.lindner.masterarbeit.epa.features.statistics

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.traces.TraceIndexingVisitor
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class PartitionsEventDistributionsVisitor<T>(
    traceIndexingVisitor: TraceIndexingVisitor<T>,
    private val progressCallback: EpaProgressCallback? = null
) : AutomatonVisitor<T> where T : Comparable<T> {
    private val statesByPartition = HashMap<Int, Set<State>>()

    val epaService = EpaService<T>()
    lateinit var epa: ExtendedPrefixAutomaton<T>
    private val countOfTracesEndingInPartition = HashMap<Int, Int>()
    private val allLastEventForEachTrace = traceIndexingVisitor.getAllTraces().map { caseIdentifier ->
        traceIndexingVisitor.getTraceByCaseIdentifierSortedByTimestamp(caseIdentifier).last()
    }.toSet()

    override fun onStart(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        epa = extendedPrefixAutomaton
        extendedPrefixAutomaton.getAllPartitions().forEach { c ->
            countOfTracesEndingInPartition[c] = 0
        }
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int
    ) {
        val states = statesByPartition[extendedPrefixAutomaton.partition(state)] ?: emptySet()

        statesByPartition[extendedPrefixAutomaton.partition(state)] = states.plus(epaService.getPathToRoot(state))
    }

    override fun onProgress(current: Long, total: Long) {
        progressCallback?.onProgress(current, total, "Relative partition frequency")
    }

    fun report(path: String) {
        // needed for faster computation
        val partitionByEvent = buildMap {
            epa.states.forEach { state ->
                val partition = epa.partition(state)
                val seq = epa.sequence(state)
                seq.forEach { event ->
                    put(event, partition)
                }
            }
        }

        // count traces ending in each partition
        allLastEventForEachTrace.forEach { event ->
            partitionByEvent[event]?.let { partition ->
                countOfTracesEndingInPartition[partition] =
                    (countOfTracesEndingInPartition[partition] ?: 0) + 1
            }
        }


        csvWriter().open(path) {
            writeRow("partition", "states", "traces")

            statesByPartition.forEach { (c, states) ->
                writeRow(c, states.size, countOfTracesEndingInPartition[c] ?: 0)
            }
        }
    }
}