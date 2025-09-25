package moritz.lindner.masterarbeit.epa.visitor.statistics

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequencyVisitor
import org.junit.jupiter.api.Test
import java.io.File

class NormalizedPartitionFrequencyVisitorTest {
    @Test
    fun `must return correct values for sample`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val frequencyVisitor = NormalizedPartitionFrequencyVisitor<Long>()

        epa.acceptDepthFirst(frequencyVisitor)

        val frequency = frequencyVisitor.build()

        val actual =
            epa.getAllPartitions().joinToString("\n") { parition ->
                "$parition: ${frequency.frequencyByPartition(parition)}"
            }

        expectSelfie(actual).toMatchDisk()
    }
}
