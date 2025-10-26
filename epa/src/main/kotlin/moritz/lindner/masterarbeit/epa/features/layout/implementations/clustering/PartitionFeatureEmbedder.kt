package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.partitioncombination.PartitionCombiner

class PartitionFeatureEmbedder() {

    fun computeEmbedding(epa: ExtendedPrefixAutomaton<Long>) {

        val partitionCombiner = PartitionCombiner<Long>()
        epa.acceptDepthFirst(partitionCombiner)
        val statePartitions = partitionCombiner.getStatePartitions()

        val partitions = epa.states.map { state ->
            epa.partition(state)
        }.toSet()

        partitions.map { c ->
//            val statesOfPartition = statePartitions.getPartitions()
            val features = mutableListOf<Double>()

            //partition length (basically seq.length)


            //depth of finale state in partition
            //total state count (including parent partitions)
            //total event count (including parent partitions)
            //total trace count (including parent partitions)
            //splitting factor (how many new different partitions are created along the path)
            //total combined cycle time (including parent partitions)
            //has repatitions (including parent partitions)
            //unique activities (including parent partitions) one hot encoded
            //prefix seq (n-gram encoded) (including parent partitions)
            //activity Count/Occurrence Encoding (including parent partitions)
            //cycle time variance
            //Median cycle time
            //depth of partition creation
            //Activity diversity (Shannon entropy)
            //Cycle time skewness: Is the distribution symmetric or does it have a long tail? Distinguishes "consistently fast with rare slow cases" from "consistently slow" Formula: E[(X - μ)³] / σ³
            //Lempel-Ziv Complexity LZ76 Algorithm (Most Common for Complexity Measurement)
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

