package moritz.lindner.masterarbeit.epa.features.paths

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
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
        val sut = DynamicPaths<Long>()

        val start = epa.getStateByName("[c] -> d")!!
        val end = epa.getStateByName("[b] -> d")!!

        val result = sut.getPathBetween(start, end)

        expectSelfie(result.value.joinToString(",")).toMatchDisk()
    }
}