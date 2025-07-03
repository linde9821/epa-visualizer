package moritz.lindner.masterarbeit.epa.features.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

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
class NormalizedPartitionFrequencyVisitor<T : Comparable<T>> : AutomatonVisitor<T> {
    private val relativeFrequencyByPartition = HashMap<Int, Float>()
    private var allEvents = 0

    /**
     * Returns the normalized frequency of events for the given partition.
     *
     * @param c The partition index.
     * @return The frequency as a float between 0.0 and 1.0.
     * @throws NullPointerException if the partition was not visited.
     */
    fun frequencyByPartition(c: Int): Float = relativeFrequencyByPartition[c]!!

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        val statesByPartition = extendedPrefixAutomaton.states.groupBy { extendedPrefixAutomaton.partition(it) }

        val frequencyByPartition =
            statesByPartition.mapValues { (partition, states) ->
                states.sumOf { extendedPrefixAutomaton.sequence(it).size }
            }

        relativeFrequencyByPartition.putAll(
            frequencyByPartition.mapValues { (partition, frequency) ->
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

    /**
     * Returns the minimum normalized frequency across all partitions.
     */
    fun min(): Float = relativeFrequencyByPartition.values.min()

    /**
     * Returns the maximum normalized frequency across all partitions.
     */
    fun max(): Float = relativeFrequencyByPartition.values.max()
}
