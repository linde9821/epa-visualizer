package moritz.lindner.masterarbeit.epa.features.paths

import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class DynamicPathsTest {
    val epa =
        EpaFromXesBuilder<Long>()
            .setFile(File("./src/test/resources/sample.xes"))
            .setEventLogMapper(SampleEventMapper())
            .build()

    @Test
    fun `must be able to compute expected path`() {
        val sut = DynamicPaths(epa)

        val start = epa.getStateByName("[c] -> d")!!
        val end = epa.getStateByName("[b] -> d")!!

        sut.getPathBetween(start, end)

    }
}