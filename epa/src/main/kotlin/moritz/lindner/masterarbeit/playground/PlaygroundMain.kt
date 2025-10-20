package moritz.lindner.masterarbeit.playground

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2018ChallengeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.layout.implementations.semanticlayout.ClusteringLayout
import java.io.File

fun main() {
    val logger = KotlinLogging.logger {}

    val sample = File("./data/eventlogs/sample.xes") to SampleEventMapper()
    File("./data/eventlogs/sample2.xes") to SampleEventMapper()
    File("./data/eventlogs/loops.xes") to SampleEventMapper()
    val offer2017 = File("./data/eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to BPI2017OfferChallengeEventMapper()
    val challenge2017 = File("./data/eventlogs/BPI Challenge 2017.xes.gz") to BPI2017ChallengeEventMapper()
    val challange2018 = File("./data/eventlogs/BPI Challenge 2018.xes.gz") to BPI2018ChallengeMapper()

    val (file, mapper) = sample

    logger.info { "Parsing ${file.absolutePath}" }

    val callback = object : EpaProgressCallback {
        override fun onProgress(current: Long, total: Long, task: String) {
            if (current % 100 == 0L) {
                println("$task: $current/$total")
            }
        }
    }
    val epa =
        EpaFromXesBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .setProgressCallback(callback)
            .build()

//    val simplifiedEpa = EpaService<Long>()
//        .applyFilters(epa, listOf(StateFrequencyFilter(0.001f)), callback)
//
//    logger.info { "build EPA successfully" }
//
//    val embedder = DL4JGraphEmbedder<Long>(simplifiedEpa)
//    val embeddings = embedder.computeEmbeddings()
//    println(embeddings.map { (k, v) ->
//        v.toList().joinToString(",") { it.toString() }
//    })
    val clusteringLayout = ClusteringLayout(
        epa
    )


    val layout = clusteringLayout.build()
}
