package moritz.lindner.masterarbeit.epa.visitor.statistics

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequencyVisitor
import org.junit.jupiter.api.Test
import java.io.File

class NormalizedPartitionFrequencyVisitorTest {
    @Test
    fun `must return correct values for sample`() {
        val epa =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val frequencyVisitor = NormalizedPartitionFrequencyVisitor<Long>()

        epa.acceptDepthFirst(frequencyVisitor)

        val actual =
            epa.getAllPartitions().joinToString("\n") { parition ->
                "$parition: ${frequencyVisitor.frequencyByPartition(parition)}"
            }

        expectSelfie(actual).toMatchDisk()
    }
}
