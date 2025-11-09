package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

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