package moritz.lindner.masterarbeit.playground

import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.export.EpaTikzExporter
import java.io.File

fun main() {
    val sample = File("./data/eventlogs/sample.xes") to SampleEventMapper()
    val (file, mapper) = sample
    val epa =
        EpaFromXesBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()

    val dotExport = EpaTikzExporter<Long>()
    epa.acceptDepthFirst(dotExport)
    saveToFile(dotExport.tikz)
}

fun saveToFile(content: String) {
    val file = File("./export.tikz")
    file.writeText(content)
}
