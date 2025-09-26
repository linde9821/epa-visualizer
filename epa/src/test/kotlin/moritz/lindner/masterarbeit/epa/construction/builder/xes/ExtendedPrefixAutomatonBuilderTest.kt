package moritz.lindner.masterarbeit.epa.construction.builder.xes

import com.diffplug.selfie.Selfie.expectSelfie
import org.junit.jupiter.api.Test
import java.io.File

class ExtendedPrefixAutomatonBuilderTest {
    @Test
    fun `must create expected epa`() {
        val sut =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        expectSelfie(sut.toString()).toMatchDisk()
    }
}
