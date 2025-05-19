package moritz.lindner.masterarbeit.epa.visitor.dot

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import org.junit.jupiter.api.Test
import java.io.File

class DotExportVisitorTest {
    @Test
    fun `must create correct dot export`() {
        val builder = ExtendedPrefixAutomataBuilder<Long>()

        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())

        val epa = builder.build()

        val sut = DotExportVisitor<Long>()

        epa.acceptDepthFirst(sut)

        val result = sut.dot

        expectSelfie(result).toMatchDisk()
    }

    @Test
    fun `must create correct dot with breath first visit`() {
        val builder = ExtendedPrefixAutomataBuilder<Long>()

        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())

        val epa = builder.build()

        val sut = DotExportVisitor<Long>()

        epa.acceptBreadthFirst(sut)

        val result = sut.dot

        expectSelfie(result).toMatchDisk()
    }
}
