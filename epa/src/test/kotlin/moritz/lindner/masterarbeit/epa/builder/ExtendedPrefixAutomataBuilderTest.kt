package moritz.lindner.masterarbeit.epa.builder

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import org.junit.jupiter.api.Test
import java.io.File

class ExtendedPrefixAutomataBuilderTest {
    @Test
    fun `must create expected epa`() {
        val sut =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        expectSelfie(sut.toString()).toMatchDisk()
    }
}
