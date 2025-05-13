package moritz.lindner.masterarbeit

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.drawing.tree.TreeBuildingVisitor
import moritz.lindner.masterarbeit.epa.builder.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2018ChallangeMapper
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitorProgressBar
import moritz.lindner.masterarbeit.epa.visitor.DotExporter
import moritz.lindner.masterarbeit.epa.visitor.StatisticsVisitor
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

    val (file, mapper) = loops

    logger.info { "Parsing ${file.absolutePath}" }

    val epa =
        ExtendedPrefixAutomataBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()

    logger.info { "build EPA successfully" }

    val visitor = DotExporter(epa)
    epa.acceptDepthFirst(visitor)
    File("./dia.dot").writeText(visitor.buildDot())
    logger.info { "\nWrote dia to ${file.absolutePath} of size ${file.length()} bytes" }

    logger.info { "Statistics:" }
    val visitor1 = StatisticsVisitor(epa)
    val statisticsVisitor = AutomataVisitorProgressBar(visitor1, "statistics")
    epa.acceptDepthFirst(statisticsVisitor)
    logger.info { visitor1.report() }
    val treeBuildingVisitor = TreeBuildingVisitor<Long>()

//    epa.acceptBreadthFirst(
//        PrintingVisitor(
//            printTransition = false,
//            printEvent = false,
//        ),
//    )
}
