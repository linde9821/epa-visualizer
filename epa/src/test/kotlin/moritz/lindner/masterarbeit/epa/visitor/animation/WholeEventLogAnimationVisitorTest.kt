package moritz.lindner.masterarbeit.epa.visitor.animation

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import org.junit.jupiter.api.Test
import java.io.File

class WholeEventLogAnimationVisitorTest {
    @Test
    fun `must create a correct animation for a given event log`() {
        val builder = ExtendedPrefixAutomataBuilder<Long>()

        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())

        val epa = builder.build()

        val sut = WholeEventLogAnimationVisitor<Long>("1")

        epa.acceptDepthFirst(sut)

        val result = sut.build(1L, Long::plus)

        expectSelfie(result.toString()).toMatchDisk()
    }
}
