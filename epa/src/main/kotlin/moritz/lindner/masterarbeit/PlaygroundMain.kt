package moritz.lindner.masterarbeit

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.builder.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2018ChallangeMapper
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.visitor.dot.DotExportVisitor
import java.io.File

fun main() {
    val logger = KotlinLogging.logger {}

    val sample = File("./eventlogs/sample.xes") to SampleEventMapper()
    val sample2 = File("./eventlogs/sample2.xes") to SampleEventMapper()
    val loops = File("./eventlogs/loops.xes") to SampleEventMapper()
    val challenge2017Offers =
        File("./eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to BPI2017OfferChallengeEventMapper()
    val challenge2017 =
        File("./eventlogs/BPI Challenge 2017.xes.gz") to BPI2017ChallengeEventMapper()
    val challenge2018 = File("./epa/src/main/resources/eventlogs/BPI Challenge 2018.xes.gz") to BPI2018ChallangeMapper()

    val (file, mapper) = challenge2017

    logger.info { "Parsing ${file.absolutePath}" }

    val epa =
        ExtendedPrefixAutomataBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()

    val dot = DotExportVisitor<Long>()
    epa.acceptDepthFirst(dot)

    File("./new.dot").writeText(dot.dot)

    logger.info { "build EPA successfully" }
}
