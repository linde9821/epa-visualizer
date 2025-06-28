package moritz.lindner.masterarbeit

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.builder.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2018ChallangeMapper
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.visitor.statistics.PartitionsAtDepthVisitor
import java.io.File

fun main() {
    val logger = KotlinLogging.logger {}

    val sample = File("./epa/src/main/resources/eventlogs/sample.xes") to SampleEventMapper()
    val sample2 = File("./epa/src/main/resources/eventlogs/sample2.xes") to SampleEventMapper()
    val loops = File("./epa/src/main/resources/eventlogs/loops.xes") to SampleEventMapper()
    val challenge2017Offers =
        File("./epa/src/main/resources/eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to BPI2017OfferChallengeEventMapper()
    val challenge2017 =
        File("./epa/src/main/resources/eventlogs/BPI Challenge 2017.xes.gz") to BPI2017ChallengeEventMapper()
    val challenge2018 = File("./epa/src/main/resources/eventlogs/BPI Challenge 2018.xes.gz") to BPI2018ChallangeMapper()

    val (file, mapper) = challenge2018

    logger.info { "Parsing ${file.absolutePath}" }

    val epa =
        ExtendedPrefixAutomataBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()
//
//    val filteredEpa =
//        ActivityFilter<Long>(
//            hashSetOf(
//                Activity("a"),
//                Activity("b"),
//                Activity("c"),
//            ),
//        ).apply(epa)
//
//    val dot1 = DotExportVisitor<Long>()
//    val dot2 = DotExportVisitor<Long>()
//    epa.copy().acceptDepthFirst(dot1)
//    filteredEpa.acceptDepthFirst(dot2)

//    File("./test1.dot").writeText(dot1.dot)
//    File("./test2.dot").writeText(dot2.dot)

    val foo = PartitionsAtDepthVisitor<Long>()
    epa.copy().acceptDepthFirst(foo)

    foo.report("./results.csv")

    logger.info { "build EPA successfully" }
}
