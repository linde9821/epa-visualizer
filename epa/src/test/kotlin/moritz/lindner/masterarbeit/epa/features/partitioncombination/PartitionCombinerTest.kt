package moritz.lindner.masterarbeit.epa.features.partitioncombination

import com.diffplug.selfie.Selfie
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import org.junit.jupiter.api.Test
import java.io.File


class PartitionCombinerTest {
    @Test
    fun `must create correct parition relation`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = PartitionCombiner<Long>()

        epa.acceptDepthFirst(sut)

        val result = sut.getStatePartitions()

        Selfie.expectSelfie(result.toString()).toMatchDisk()
    }
}