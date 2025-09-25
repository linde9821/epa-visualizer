package moritz.lindner.masterarbeit.epa.visitor.case

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.animation.SingleCaseAnimationBuilder
import org.junit.jupiter.api.Test
import java.io.File

class SingleCaseAnimationVisitorTest {
    @Test
    fun `must create a correct animation for a given case`() {
        val builder = EpaFromXesBuilder<Long>()

        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())

        val epa = builder.build()

        val sut = SingleCaseAnimationBuilder<Long>("1")

        epa.acceptDepthFirst(sut)

        val result = sut.build()

        expectSelfie(result.toString()).toMatchDisk()
    }
}
