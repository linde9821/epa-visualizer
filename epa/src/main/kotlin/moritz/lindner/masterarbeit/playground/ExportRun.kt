package moritz.lindner.masterarbeit.playground

import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.export.EpaTikzExporter
import moritz.lindner.masterarbeit.epa.features.filter.CompressionFilter
import java.io.File

fun main() {
    val sample = File("./data/eventlogs/ma_sample.xes") to SampleEventMapper()
    val (file, mapper) = sample
    val epa =
        EpaFromXesBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()

//    val filteredEpa = EpaService<Long>().applyFilters(epa, listOf(CompressionFilter<Long>()))

    val export = EpaTikzExporter<Long>()
    epa.acceptDepthFirst(export)
    saveToFile(export.tikz)
}

fun saveToFile(content: String) {
    val file = File("./export.tikz")
    file.writeText(content)
}
