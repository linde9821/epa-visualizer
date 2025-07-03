package moritz.lindner.masterarbeit.epa.visitor.dot

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomatonBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.dot.DotExport
import org.junit.jupiter.api.Test
import java.io.File

class DotExportVisitorTest {
    @Test
    fun `must create correct dot export`() {
        val builder = ExtendedPrefixAutomatonBuilder<Long>()

        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())

        val epa = builder.build()

        val sut = DotExport<Long>()

        epa.acceptDepthFirst(sut)

        val result = sut.dot

        expectSelfie(result).toMatchDisk()
    }

    @Test
    fun `must create correct dot with breath first visit`() {
        val builder = ExtendedPrefixAutomatonBuilder<Long>()

        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())

        val epa = builder.build()

        val sut = DotExport<Long>()

        epa.acceptBreadthFirst(sut)

        val result = sut.dot

        expectSelfie(result).toMatchDisk()
    }
}
