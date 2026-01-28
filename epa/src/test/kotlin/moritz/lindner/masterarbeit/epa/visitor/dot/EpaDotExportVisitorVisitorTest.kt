package moritz.lindner.masterarbeit.epa.visitor.dot

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.export.EpaDotExportVisitor
import org.junit.jupiter.api.Test
import java.io.File

class EpaDotExportVisitorVisitorTest {
    @Test
    fun `must create correct dot export`() {
        val builder = EpaFromXesBuilder<Long>()

        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())

        val epa = builder.build()

        val sut = EpaDotExportVisitor<Long>()

        epa.acceptDepthFirst(sut)

        val result = sut.dot

        expectSelfie(result).toMatchDisk()
    }

    @Test
    fun `must create correct dot with breath first visit`() {
        val builder = EpaFromXesBuilder<Long>()

        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())

        val epa = builder.build()

        val sut = EpaDotExportVisitor<Long>()

        epa.acceptBreadthFirst(sut)

        val result = sut.dot

        expectSelfie(result).toMatchDisk()
    }
}
