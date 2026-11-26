package moritz.lindner.masterarbeit.epa.util

import com.jsoizo.kotlincsv.csvWriter
import com.jsoizo.kotlincsv.writer.writeToFile
import java.io.File

/** Collects CSV rows, converting each cell with [toString] (null becomes an empty cell). */
class CsvRowCollector {
    internal val rows = mutableListOf<List<String>>()

    fun writeRow(vararg cells: Any?) = writeRow(cells.asList())

    fun writeRow(cells: List<Any?>) {
        rows += cells.map { it?.toString() ?: "" }
    }

    fun writeRows(rows: List<List<Any?>>) = rows.forEach(::writeRow)
}

/** Writes the rows produced by [block] to [file] using kotlin-csv's default dialect. */
fun writeCsv(file: File, block: CsvRowCollector.() -> Unit) {
    csvWriter().writeToFile(CsvRowCollector().apply(block).rows, file)
}

fun writeCsv(path: String, block: CsvRowCollector.() -> Unit) = writeCsv(File(path), block)
