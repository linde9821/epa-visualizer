package moritz.lindner.masterarbeit.epa.construction.parser

import me.tongfei.progressbar.ConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import org.deckfour.xes.`in`.XesXmlGZIPParser
import org.deckfour.xes.`in`.XesXmlParser
import org.deckfour.xes.model.XLog
import java.io.File
import java.io.InputStream

/**
 * A file parser for XES logs that supports both plain XML and GZIP-compressed formats,
 * with integrated progress bar reporting.
 *
 * This class dynamically selects the appropriate parser strategy based on file extension
 * and wraps parsing with a visual progress bar.
 */
class EPAXesParser : XesXmlParser() {
    private val parserStrategyByExtension = mapOf("xes" to XesXmlParser(), "gz" to XesXmlGZIPParser())
    private lateinit var parser: XesXmlParser

    override fun name(): String = "XES XML with progress"

    override fun description(): String = "Reads XES models from plain XML or GZ XML files with progress report "

    override fun author(): String = "Moritz Lindner"

    /**
     * Determines whether this parser can parse the given file.
     *
     * Supports files ending in `.xes` and `.gz`.
     *
     * @param file The file to test.
     * @return True if a parser strategy is available and can parse the file; false otherwise.
     */
    override fun canParse(file: File): Boolean =
        parserStrategyByExtension[file.extension]?.also { parser = it }?.canParse(file) ?: false

    /**
     * Parses the given input stream into a list of [XLog]s, showing a progress bar while reading.
     *
     * @param is The input stream to parse (must not be null).
     * @return A list of parsed XES logs.
     * @throws IllegalArgumentException If the input stream is null.
     */
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
