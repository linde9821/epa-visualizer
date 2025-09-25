package moritz.lindner.masterarbeit.playground

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2018ChallangeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.features.dot.DotExport
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequencyVisitor
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor
import java.io.File

fun main() {
    val logger = KotlinLogging.logger {}

    val sample = File("./data/eventlogs/sample.xes") to SampleEventMapper()
    File("./data/eventlogs/sample2.xes") to SampleEventMapper()
    File("./data/eventlogs/loops.xes") to SampleEventMapper()
    File("./data/eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to BPI2017OfferChallengeEventMapper()
    File("./data/eventlogs/BPI Challenge 2017.xes.gz") to BPI2017ChallengeEventMapper()
    File("./epa/src/main/resources/eventlogs/BPI Challenge 2018.xes.gz") to BPI2018ChallangeMapper()

    val (file, mapper) = sample

    logger.info { "Parsing ${file.absolutePath}" }

    val epa =
        EpaFromXesBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()

    logger.info { "build EPA successfully" }

    val paritionFrequencyFilter = NormalizedPartitionFrequencyVisitor<Long>()

    epa.acceptDepthFirst(paritionFrequencyFilter)
}
