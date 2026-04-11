package moritz.lindner.masterarbeit.playground

import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.export.EpaTikzExporter
import java.io.File

fun main() {
    val sample = File("./data/eventlogs/ma_sample.xes") to SampleEventMapper()
    val (file, mapper) = sample


    (1 until 25).forEach {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(file)
                .setEventLogMapper(mapper)
                .parseNEvents(it)
                .build()
        val export = EpaTikzExporter<Long>()
        epa.acceptDepthFirst(export)
        saveToFile(export.tikz, "/Users/moritzlindner/programming/masterarbeit/epa-visualizer/data/images/tikz/Animation_${it}.tikz")
    }
}