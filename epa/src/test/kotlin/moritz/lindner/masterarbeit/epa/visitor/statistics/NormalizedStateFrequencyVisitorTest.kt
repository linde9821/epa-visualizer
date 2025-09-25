package moritz.lindner.masterarbeit.epa.visitor.statistics

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequencyVisitor
import org.junit.jupiter.api.Test
import java.io.File

class NormalizedStateFrequencyVisitorTest {
    @Test
    fun `must return correct values for sample`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val frequencyVisitor = NormalizedStateFrequencyVisitor<Long>()

        epa.acceptDepthFirst(frequencyVisitor)

        val frequency = frequencyVisitor.build()

        val actual =
            epa.states.joinToString("\n") { state ->
                "$state: ${frequency.frequencyByState(state)}"
            }

        expectSelfie(actual).toMatchDisk()
    }
}
