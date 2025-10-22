package moritz.lindner.masterarbeit.epa.features.subtree

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class SubtreeSizeVisitorTest {

    @Test
    fun `must create correct sizes`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = SubtreeSizeVisitor()
        epa.acceptDepthFirst(sut)

        val actual = sut.sizeByState

        expectSelfie(actual.toList().joinToString()).toMatchDisk()
    }
}