package moritz.lindner.masterarbeit.epa.features.statistics

/**
 * Contains normalized frequency statistics for partitions in an Extended
 * Prefix Automaton.
 *
 * @param relativeFrequencyByPartition Map of partition indices to their
 *    normalized frequencies.
 */
class NormalizedPartitionFrequency(
    private val relativeFrequencyByPartition: Map<Int, Float>
) {
    /**
     * Returns the normalized frequency of events for the given partition.
     *
     * @param c The partition index.
     * @return The frequency as a float between 0.0 and 1.0.
     * @throws IllegalStateException if the partition was not visited.
     */
    fun frequencyByPartition(c: Int): Float =
        relativeFrequencyByPartition[c] ?: throw IllegalStateException("Partition $c was not visited")

    /** Returns the minimum normalized frequency across all partitions. */
    fun min(): Float = relativeFrequencyByPartition.values.min()

    /** Returns the maximum normalized frequency across all partitions. */
    fun max(): Float = relativeFrequencyByPartition.values.max()

    /**
     * Returns all partitions sorted by their normalized frequency in
     * descending order.
     *
     * @return A list of partition indices ordered from highest to lowest
     *    frequency.
     */
    fun getPartitionsSortedByFrequencyDescending(): List<Int> {
        return relativeFrequencyByPartition
            .toList()
            .sortedByDescending { (_, value) -> value }
            .map { (key, _) -> key }
    }
}