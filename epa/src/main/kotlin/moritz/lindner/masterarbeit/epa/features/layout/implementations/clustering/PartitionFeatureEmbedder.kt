package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.partitioncombination.PartitionCombiner
import moritz.lindner.masterarbeit.epa.features.partitioncombination.StatePartitionsCollection
import java.util.concurrent.atomic.AtomicInteger

class PartitionFeatureEmbedder(
    private val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val config: PartitionEmbedderConfig,
    private val backgroundDispatcher: CoroutineDispatcher,
    private val progressCallback: EpaProgressCallback?,
) {

    private val logger = KotlinLogging.logger { }

    fun computeEmbedding(): Map<Int, DoubleArray> {
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
            progressCallback = progressCallback
        )

        val ngramEncoder = NgramEncoder(
            config = NgramConfig(),
            ngramVocabulary = buildNgramVocabularyFromData(
                statePartitions = statePartitions,
                config = NgramConfig()
            )
        )

        val counter = AtomicInteger(0)

        val allPartitions = statePartitions.getAllPartitions()
        val total = allPartitions.size

        return runBlocking {
            val chunkSize = 100

            allPartitions.chunked(chunkSize).mapIndexed { index, chunk ->
                async(backgroundDispatcher) {
                    logger.info { "Computing chunk $index" }
                    chunk.map { c ->
                        val currentCount = counter.incrementAndGet()
                        progressCallback?.onProgress(currentCount, total, "Partition Feature Embedding")

                        val features = mutableListOf<Double>()
                        val statesOfCurrentPartition = statePartitions.getStates(c).sortedBy { epaService.getDepth(it) }
                        val activitySequence = statesOfCurrentPartition.mapNotNull { it as? State.PrefixState }
                            .map { it.via.name }

                        //total state count (including parent partitions)
                        if (config.useTotalStateCount) {
                            val totalStateCount = statesOfCurrentPartition.size
                            features.add(totalStateCount.toDouble())
                        }

                        //total event count (including parent partitions)
                        val totalEvents = statesOfCurrentPartition.flatMap { state ->
                            extendedPrefixAutomaton.sequence(state)
                        }.distinct()
                        if (config.useTotalEventCount) {
                            val totalEventCount = totalEvents.count()
                            features.add(totalEventCount.toDouble())
                        }

                        //total trace count (including parent partitions)
                        if (config.useTotalTraceCount) {
                            val totalTraceCount = totalEvents.distinctBy { it.caseIdentifier }.distinct().size
                            features.add(totalTraceCount.toDouble())
                        }

                        //depth of finale state in partition
                        if (config.useDeepestDepth) {
                            val deepestDepth = statesOfCurrentPartition.maxOf { state ->
                                epaService.getDepth(state)
                            }
                            features.add(deepestDepth.toDouble())
                        }

                        //splitting factor (how many new different partitions are created along the path)
                        if (config.useSplittingFactor) {
                            features.add(statePartitions.splittingFactor(c).toDouble())
                        }

                        //has repetitions (including parent partitions)
                        if (config.useHasRepetition) {
                            val hasRepetition = statePartitions.hasRepetition(c)
                            if (hasRepetition) {
                                features.add(1.0)
                            } else {
                                features.add(0.0)
                            }
                        }

                        //total combined cycle time (including parent partitions)
                        if (config.useCombinedCycleTime) {
                            val combinedCycleTime = statesOfCurrentPartition.map { cycleTimes[it]!! }.sum()
                            features.add(combinedCycleTime.toDouble())
                        }

                        if (config.useActivitySequenceEncoding) {
                            val activityEncoding = ngramEncoder.encode(activitySequence)
                            features.addAll(activityEncoding.toList())
                        }

                        //Lempel-Ziv Complexity LZ76 Algorithm (Most Common for Complexity Measurement)
                        if (config.useLempelZivComplexity) {
                            val complexity =
                                lempelZivComplexity(activitySequence)
                            features.add(complexity.toDouble())
                        }

                        val result = DoubleArray(features.size)
                        features.forEachIndexed { index, value ->
                            result[index] = value
                        }

                        c to result
                    }.also {
                        logger.info { "Finished chunk $chunk" }
                    }
                }
            }.awaitAll().flatten().toMap()
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

    private fun buildNgramVocabularyFromData(
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

    private fun extractActivitySequence(states: List<State>): List<String> {
        return states.mapNotNull { (it as? State.PrefixState)?.via?.name }
    }
}

