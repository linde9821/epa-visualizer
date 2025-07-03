package moritz.lindner.masterarbeit.playground

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.construction.builder.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.BPI2018ChallangeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.dot.DotExport
import java.io.File

fun main() {
    val logger = KotlinLogging.logger {}

    val sample = File("./data/eventlogs/sample.xes") to SampleEventMapper()
    val sample2 = File("./data/eventlogs/sample2.xes") to SampleEventMapper()
    val loops = File("./data/eventlogs/loops.xes") to SampleEventMapper()
    val challenge2017Offers =
        File("./data/eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to BPI2017OfferChallengeEventMapper()
    val challenge2017 =
        File("./data/eventlogs/BPI Challenge 2017.xes.gz") to BPI2017ChallengeEventMapper()
    val challenge2018 = File("./epa/src/main/resources/eventlogs/BPI Challenge 2018.xes.gz") to BPI2018ChallangeMapper()

    val (file, mapper) = sample

    logger.info { "Parsing ${file.absolutePath}" }

    val epa =
        ExtendedPrefixAutomataBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()

    val dot = DotExport<Long>()
    epa.acceptDepthFirst(dot)

    File("./dia.dot").writeText(dot.dot)

    logger.info { "build EPA successfully" }
}
