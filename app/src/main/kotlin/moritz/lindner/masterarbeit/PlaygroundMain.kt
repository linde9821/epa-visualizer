package moritz.lindner.masterarbeit

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.builder.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2018
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomateBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.visitor.DotExporter
import moritz.lindner.masterarbeit.epa.visitor.StatisticsVisitor
import java.io.File

fun main() {
    val logger = KotlinLogging.logger {}

    val sample = File("./app/src/main/resources/sample.xes") to SampleEventMapper()
    val sample2 = File("./app/src/main/resources/sample2.xes") to SampleEventMapper()
    val loops = File("./app/src/main/resources/loops.xes") to SampleEventMapper()
    val offerChallenge =
        File("./app/src/main/resources/BPI Challenge 2017 - Offer log.xes") to BPI2017OfferChallengeEventMapper()
    val challenge2017 = File("./app/src/main/resources/BPI Challenge 2017.xes") to BPI2017ChallengeEventMapper()
    val challenge2018 = File("./app/src/main/resources/BPI Challenge 2018.xes.gz") to BPI2018()

    val (file, mapper) = sample

    logger.info { "Parsing ${file.absolutePath}" }

    val epa =
        ExtendedPrefixAutomateBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()

    logger.info { "build EPA successfully" }

    val visitor = DotExporter(epa)
    epa.acceptDepthFirst(visitor)
    File("./dia.dot").writeText(visitor.buildDot())
    logger.info { "\nWrote dia to ${file.absolutePath} of size ${file.length()} bytes" }

    logger.info { "Statistics:" }
    val statisticsVisitor = StatisticsVisitor(epa)
    epa.acceptDepthFirst(statisticsVisitor)
    logger.info { statisticsVisitor.report() }

    logger.info { "depth first" }
//    epa.acceptDepthFirst(
//        PrintingVisitor(
//            printTransition = false,
//            printEvent = false,
//        ),
//    )

    logger.info { "breadth first" }
//    epa.acceptBreadthFirst(
//        PrintingVisitor(
//            printTransition = false,
//            printEvent = false,
//        ),
//    )
}
