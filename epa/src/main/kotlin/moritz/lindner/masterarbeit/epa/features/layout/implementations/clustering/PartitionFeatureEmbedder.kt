package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.partitioncombination.PartitionCombiner

class PartitionFeatureEmbedder() {

    fun computeEmbedding(epa: ExtendedPrefixAutomaton<Long>): Map<Int, DoubleArray> {
        val epaService = EpaService<Long>()
        val partitionCombiner = PartitionCombiner<Long>()
        epa.acceptDepthFirst(partitionCombiner)
        val statePartitions = partitionCombiner.getStatePartitions()

        return statePartitions.getAllPartitions().associateWith { c ->
            val features = mutableListOf<Double>()
            val statesOfPartition = statePartitions.getStates(c)

            //total state count (including parent partitions)
            val totalStateCount = statesOfPartition.size
            features.add(totalStateCount.toDouble())

            //total event count (including parent partitions)
            val totalEvents = statesOfPartition.flatMap { state ->
                epa.sequence(state)
            }.distinct()
            val totalEventCount = totalEvents.count()
            features.add(totalEventCount.toDouble())

            //total trace count (including parent partitions)
            val totalTraceCount = totalEvents.distinctBy { it.caseIdentifier }.distinct().size
            features.add(totalTraceCount.toDouble())

            //depth of finale state in partition
            val deepestDepth = statesOfPartition.maxOf { state ->
                epaService.getDepth(state)
            }
            features.add(deepestDepth.toDouble())

            //splitting factor (how many new different partitions are created along the path)
            features.add(statePartitions.splittingFactor(c).toDouble())

            //has repetitions (including parent partitions)
            val hasRepetition = statePartitions.hasRepetition(c)
            if (hasRepetition) {
                features.add(1.0)
            } else {
                features.add(0.0)
            }

            //partition length (basically seq.length)
            //total combined cycle time (including parent partitions)
            //unique activities (including parent partitions) one hot encoded
            //prefix seq (n-gram encoded) (including parent partitions)
            //activity Count/Occurrence Encoding (including parent partitions)
            //cycle time variance
            //Median cycle time
            //depth of partition creation
            //Activity diversity (Shannon entropy)
            //Cycle time skewness: Is the distribution symmetric or does it have a long tail? Distinguishes "consistently fast with rare slow cases" from "consistently slow" Formula: E[(X - μ)³] / σ³
            //Lempel-Ziv Complexity LZ76 Algorithm (Most Common for Complexity Measurement)

            val result = DoubleArray(features.size)
            features.forEachIndexed { index, value ->
                result[index] = value
            }
            result
        }
    }


    // https://github.com/Naereen/Lempel-Ziv_Complexity
    fun lempelZivComplexity(sequence: List<String>): Int {
        if (sequence.isEmpty()) return 0

        val subStrings = mutableSetOf<String>()
        val delimiter = "|" // separator for joining
        var ind = 0
        var inc = 1

        while (ind + inc <= sequence.size) {
            val subStr = sequence.subList(ind, ind + inc).joinToString(delimiter)

            if (subStr in subStrings) {
                inc++
            } else {
                subStrings.add(subStr)
                ind += inc
                inc = 1
            }
        }

        return subStrings.size
    }
}

