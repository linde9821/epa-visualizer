package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.partitioncombination.PartitionCombiner
import moritz.lindner.masterarbeit.epa.features.partitioncombination.StatePartitionsCollection

class PartitionFeatureEmbedder() {

    fun computeEmbedding(extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>): Map<Int, DoubleArray> {
        val epaService = EpaService<Long>()
        val partitionCombiner = PartitionCombiner<Long>()
        extendedPrefixAutomaton.acceptDepthFirst(partitionCombiner)
        val statePartitions: StatePartitionsCollection<Long> = partitionCombiner.getStatePartitions()
        val cycleTimes = epaService.computeAllCycleTimes(
            extendedPrefixAutomaton = extendedPrefixAutomaton,
            minus = Long::minus,
            average = { cycleTimes ->
                if (cycleTimes.isEmpty()) {
                    0f
                } else cycleTimes.average().toFloat()
            },
        )

        val ngramEncoder = NgramEncoder(
            config = NgramConfig(),
            ngramVocabulary = buildNgramVocabularyFromData(
                statePartitions = statePartitions,
                config = NgramConfig()
            )
        )

        return statePartitions.getAllPartitions().associateWith { c ->
            val features = mutableListOf<Double>()
            val statesOfCurrentPartition = statePartitions.getStates(c).sortedBy { epaService.getDepth(it) }
            val activitySequence = statesOfCurrentPartition.mapNotNull { it as? State.PrefixState }
                .map { it.via.name }

            //total state count (including parent partitions)
            val totalStateCount = statesOfCurrentPartition.size
            features.add(totalStateCount.toDouble())

            //total event count (including parent partitions)
            val totalEvents = statesOfCurrentPartition.flatMap { state ->
                extendedPrefixAutomaton.sequence(state)
            }.distinct() // this is quite slow
            val totalEventCount = totalEvents.count()
            features.add(totalEventCount.toDouble())

            // this is quite slow
            //total trace count (including parent partitions)
            val totalTraceCount = totalEvents.distinctBy { it.caseIdentifier }.distinct().size
            features.add(totalTraceCount.toDouble())

            //depth of finale state in partition
            val deepestDepth = statesOfCurrentPartition.maxOf { state ->
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

            //total combined cycle time (including parent partitions)
            val combinedCycleTime = statesOfCurrentPartition.map { cycleTimes[it]!! }.sum()
            features.add(combinedCycleTime.toDouble())

            val activityEncoding = ngramEncoder.encode(activitySequence)
            features.addAll(activityEncoding.toList())

            //Activity diversity (Shannon entropy)
            //Cycle time skewness: Is the distribution symmetric or does it have a long tail? Distinguishes "consistently fast with rare slow cases" from "consistently slow" Formula: E[(X - μ)³] / σ³

            //Lempel-Ziv Complexity LZ76 Algorithm (Most Common for Complexity Measurement)
            val complexity =
                lempelZivComplexity(activitySequence)
            features.add(complexity.toDouble())

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

data class NgramConfig(
    val n: Int = 3,
    val includeUnigrams: Boolean = true,
    val includeBigrams: Boolean = true,
    val maxNgramFeatures: Int = 100
)

class NgramEncoder(
    private val config: NgramConfig,
    private val ngramVocabulary: Map<String, Int>
) {

    fun encode(activitySequence: List<String>): DoubleArray {
        val ngramCounts = mutableMapOf<String, Int>()

        // Generate n-grams of different sizes
        if (config.includeUnigrams) {
            activitySequence.forEach { activity ->
                ngramCounts[activity] = ngramCounts.getOrDefault(activity, 0) + 1
            }
        }

        if (config.includeBigrams && activitySequence.size >= 2) {
            activitySequence.windowed(2).forEach { bigram ->
                val key = bigram.joinToString("->")
                ngramCounts[key] = ngramCounts.getOrDefault(key, 0) + 1
            }
        }

        if (config.n >= 3 && activitySequence.size >= config.n) {
            activitySequence.windowed(config.n).forEach { ngram ->
                val key = ngram.joinToString("->")
                ngramCounts[key] = ngramCounts.getOrDefault(key, 0) + 1
            }
        }

        // Convert to fixed-size vector
        val vector = DoubleArray(ngramVocabulary.size)
        ngramCounts.forEach { (ngram, count) ->
            ngramVocabulary[ngram]?.let { index ->
                vector[index] = count.toDouble()
            }
        }

        return vector
    }
}

// Helper to build vocabulary from actual data
fun buildNgramVocabularyFromData(
    statePartitions: StatePartitionsCollection<Long>,
    config: NgramConfig
): Map<String, Int> {
    val ngramFrequencies = mutableMapOf<String, Int>()
    val epaService = EpaService<Long>()

    statePartitions.getAllPartitions().forEach { partition ->
        val states = statePartitions.getStates(partition).sortedBy { epaService.getDepth(it) }
        val activities = extractActivitySequence(states)

        // Count all n-grams
        if (config.includeUnigrams) {
            activities.forEach { activity ->
                ngramFrequencies[activity] = ngramFrequencies.getOrDefault(activity, 0) + 1
            }
        }

        if (config.includeBigrams && activities.size >= 2) {
            activities.windowed(2).forEach { bigram ->
                val key = bigram.joinToString("->")
                ngramFrequencies[key] = ngramFrequencies.getOrDefault(key, 0) + 1
            }
        }

        if (config.n >= 3 && activities.size >= config.n) {
            activities.windowed(config.n).forEach { ngram ->
                val key = ngram.joinToString("->")
                ngramFrequencies[key] = ngramFrequencies.getOrDefault(key, 0) + 1
            }
        }
    }

    // Take top N most frequent n-grams
    return ngramFrequencies
        .entries
        .sortedByDescending { it.value }
        .take(config.maxNgramFeatures)
        .mapIndexed { index, entry -> entry.key to index }
        .toMap()
}

fun extractActivitySequence(states: List<State>): List<String> {
    return states
        .mapNotNull { (it as? State.PrefixState)?.via?.name }
}
