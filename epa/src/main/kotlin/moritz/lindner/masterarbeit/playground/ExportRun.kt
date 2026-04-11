package moritz.lindner.masterarbeit.playground

import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.export.EpaTikzExporter
import moritz.lindner.masterarbeit.epa.features.filter.CompressionFilter
import moritz.lindner.masterarbeit.epa.features.filter.PartitionFrequencyFilter
import java.io.File

fun main() {
    val sample = File("./data/eventlogs/ma_sample.xes") to SampleEventMapper()
    val (file, mapper) = sample
    val epa =
        EpaFromXesBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()

    val filteredEpa = EpaService<Long>().applyFilters(
        epa, listOf(
            PartitionFrequencyFilter(0.2f),
            CompressionFilter(),
        )
    )

    val export = EpaTikzExporter<Long>()
    filteredEpa.acceptDepthFirst(export)
    saveToFile(export.tikz)
}

fun saveToFile(content: String, filePath: String = "export.tikz") {
    val file = File(filePath)
    file.writeText(content)
}
