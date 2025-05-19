package moritz.lindner.masterarbeit.epa.parser

import me.tongfei.progressbar.ConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import org.deckfour.xes.`in`.XesXmlGZIPParser
import org.deckfour.xes.`in`.XesXmlParser
import org.deckfour.xes.model.XLog
import java.io.File
import java.io.InputStream

class EPAXesParser : XesXmlParser() {
    private val parserStrategyByExtension = mapOf("xes" to XesXmlParser(), "gz" to XesXmlGZIPParser())
    private lateinit var parser: XesXmlParser

    override fun name(): String = "XES XML with progress"

    override fun description(): String = "Reads XES models from plain XML or GZ XML files with progress report "

    override fun author(): String = "Moritz Lindner"

    override fun canParse(file: File): Boolean = parserStrategyByExtension[file.extension]?.also { parser = it }?.canParse(file) ?: false

    override fun parse(`is`: InputStream?): MutableList<XLog> {
        if (`is` == null) throw IllegalArgumentException("InputStream cannot be null")

        val wrap =
            ProgressBar.wrap(
                `is`,
                ProgressBarBuilder()
                    .showSpeed()
                    .setConsumer(ConsoleProgressBarConsumer(System.out))
                    .setTaskName("parsing xes")
                    .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                    .setMaxRenderedLength(80)
                    .setUnit("MiB", 1024 * 1024), // Set unit to MiB,
            )
        return parser.parse(wrap)
    }
}
