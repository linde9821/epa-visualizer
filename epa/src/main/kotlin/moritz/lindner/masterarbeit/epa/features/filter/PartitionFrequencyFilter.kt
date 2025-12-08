package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaFromComponentsBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback

/**
 * Filters an [ExtendedPrefixAutomaton] by removing all states and
 * transitions that belong to partitions with a normalized frequency below
 * a given [threshold].
 *
 * A state may remain, even when his partition is below the threshold, when it's a parent to a partition
 * with a higher frequency and this partition would be removed otherwise due to the unreachability which would be
 * introduced otherwise.
 *
 * Partitions are evaluated using a
 * [moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequencyVisitor],
 * and only those with frequency >= [threshold] (or partition 0, which is
 * always retained) are kept.
 *
 * @param T The timestamp type used in the automaton's events.
 * @property threshold The minimum normalized frequency a partition must
 *    have to be included.
 */
class PartitionFrequencyFilter<T : Comparable<T>>(
    private val threshold: Float,
    private val withPruning: Boolean = true
) : EpaFilter<T> {

    private val epaService = EpaService<T>()

    override val name: String
        get() = "Partition Frequency Filter"

    /**
     * Applies the frequency-based partition filtering logic to the given
     * automaton.
     *
     * @param epa The automaton to filter.
     * @return A new [ExtendedPrefixAutomaton] with only frequent partitions
     *    retained.
     */
    override fun apply(
        epa: ExtendedPrefixAutomaton<T>,
        progressCallback: EpaProgressCallback?
    ): ExtendedPrefixAutomaton<T> {
        val normalizedPartitionFrequency = epaService.getNormalizedPartitionFrequency(epa, progressCallback)
        val partitionsAboveThreshold = epa
            .getAllPartitions()
            .associateWith(normalizedPartitionFrequency::frequencyByPartition)
            .filter { (partition, frequency) -> frequency >= threshold || partition == 0 }
            .keys

        val statesInPartitionsAboveThreshold = epa.states
            .filterIndexed { index, state ->
                progressCallback?.onProgress(index, epa.states.size, "${name}: Filter states")
                val partition = epa.partition(state)
                partition in partitionsAboveThreshold
            }.toSet()

        val statesInPartitionWithParents = statesInPartitionsAboveThreshold.flatMap { state ->
            epaService.getPathToRoot(state)
        }.toSet()

        val epaBuilder = EpaFromComponentsBuilder<T>()
            .fromExisting(epa)
            .setStates(statesInPartitionWithParents)
            .pruneStatesUnreachableByTransitions(withPruning)
            .setProgressCallback(progressCallback)
            .setEventLogName(epa.eventLogName + " $name with threshold >= $threshold")

        return epaBuilder.build()
    }
}
