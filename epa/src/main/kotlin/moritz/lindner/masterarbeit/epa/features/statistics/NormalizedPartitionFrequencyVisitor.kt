package moritz.lindner.masterarbeit.epa.features.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor
import org.apache.logging.log4j.core.net.Priority

/**
 * Computes the normalized frequency of events per partition in an [ExtendedPrefixAutomaton].
 *
 * The frequency is calculated as the number of events observed in each partition
 * divided by the total number of events across all partitions. The result is a value in [0.0, 1.0].
 *
 * This visitor must be run using [ExtendedPrefixAutomaton.acceptDepthFirst] or [acceptBreadthFirst]
 * before querying the frequencies.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class NormalizedPartitionFrequencyVisitor<T : Comparable<T>>(
    private val progressCallback: EpaProgressCallback? = null
) : AutomatonVisitor<T> {
    private val relativeFrequencyByPartition = HashMap<Int, Float>()
    private var allEvents = 0

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        val statesByPartition = extendedPrefixAutomaton
            .states
            .groupBy(extendedPrefixAutomaton::partition)

        val frequencyByPartition = statesByPartition
            .mapValues { (_, states) ->
                states.sumOf { state -> extendedPrefixAutomaton.sequence(state).size }
            }

        relativeFrequencyByPartition.putAll(
            frequencyByPartition.mapValues { (_, frequency) ->
                frequency.toFloat() / allEvents
            },
        )
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        event: Event<T>,
        depth: Int,
    ) {
        allEvents++
    }

    override fun onProgress(current: Long, total: Long) {
        progressCallback?.onProgress(current, total, "Compute normalized frequency of events per partition")
    }

    fun build(): NormalizedPartitionFrequency {
        return NormalizedPartitionFrequency(relativeFrequencyByPartition)
    }
}
