package moritz.lindner.masterarbeit.playground

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2018ChallengeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.StateClusteringLayout
import moritz.lindner.masterarbeit.epa.features.statistics.PartitionsEventDistributionsVisitor
import moritz.lindner.masterarbeit.epa.features.statistics.StatesAndPartitionsByDepthVisitor
import moritz.lindner.masterarbeit.epa.features.traces.TraceIndexingVisitor
import java.io.File

fun main() {
    val logger = KotlinLogging.logger {}

    val sample = File("./data/eventlogs/sample.xes") to SampleEventMapper()
    File("./data/eventlogs/sample2.xes") to SampleEventMapper()
    File("./data/eventlogs/loops.xes") to SampleEventMapper()
    val offer2017 = File("./data/eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to BPI2017OfferChallengeEventMapper()
    val challenge2017 = File("./data/eventlogs/BPI Challenge 2017.xes.gz") to BPI2017ChallengeEventMapper()
    val challenge2018 = File("./data/eventlogs/BPI Challenge 2018.xes.gz") to BPI2018ChallengeMapper()

    val (file, mapper) = challenge2018

    logger.info { "Parsing ${file.absolutePath}" }

    val callback = EpaProgressCallback { current, total, task ->
        if (current % 100 == 0L) {
            println("$task: $current/$total")
        }
    }
    val epa =
        EpaFromXesBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .setProgressCallback(callback)
            .build()

    val foo = TraceIndexingVisitor<Long>()
    epa.acceptDepthFirst(foo)
    val v = PartitionsEventDistributionsVisitor<Long>(foo)
    epa.acceptDepthFirst(v)

    v.report("/Users/moritzlindner/programming/masterarbeit/epa-visualizer/data/statistics/partitions_distributation.csv")
}
