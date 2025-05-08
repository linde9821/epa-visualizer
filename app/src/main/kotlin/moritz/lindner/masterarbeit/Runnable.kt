package moritz.lindner.masterarbeit

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.builder.BPI2018
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomateBuilder
import moritz.lindner.masterarbeit.epa.visitor.DotExporter
import moritz.lindner.masterarbeit.epa.visitor.StatisticsVisitor
import java.io.File

fun main(args: Array<String>) {
    val logger = KotlinLogging.logger {}

    val file = File(args[0])
    val mapper = BPI2018()

    val epa =
        ExtendedPrefixAutomateBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()

    logger.info { "build EPA successfully" }

    val visitor = DotExporter(epa)
    epa.acceptDepthFirst(visitor)
    val dot = visitor.buildDot()
    File("dia.dot").writeText(dot)
    logger.info { "wrote dot to ${file.absolutePath}" }
    val statisticsVisitor = StatisticsVisitor(epa)
    epa.acceptDepthFirst(statisticsVisitor)
    logger.info { statisticsVisitor.report() }
}
