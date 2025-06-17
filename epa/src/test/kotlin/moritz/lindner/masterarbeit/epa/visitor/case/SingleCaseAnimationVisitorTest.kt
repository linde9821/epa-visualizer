package moritz.lindner.masterarbeit.epa.visitor.case

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.visitor.animation.SingleCaseAnimationVisitor
import org.junit.jupiter.api.Test
import java.io.File

class SingleCaseAnimationVisitorTest {
    @Test
    fun `must create a correct animation for a given case`() {
        val builder = ExtendedPrefixAutomataBuilder<Long>()

        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())

        val epa = builder.build()

        val sut = SingleCaseAnimationVisitor<Long>("1")

        epa.acceptDepthFirst(sut)

        val result = sut.build()

        expectSelfie(result.toString()).toMatchDisk()
    }
}
