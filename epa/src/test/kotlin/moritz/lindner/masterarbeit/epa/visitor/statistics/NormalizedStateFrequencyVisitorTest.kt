package moritz.lindner.masterarbeit.epa.visitor.statistics

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomatonBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequencyVisitor
import org.junit.jupiter.api.Test
import java.io.File

class NormalizedStateFrequencyVisitorTest {
    @Test
    fun `must return correct values for sample`() {
        val epa =
            ExtendedPrefixAutomatonBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val frequencyVisitor = NormalizedStateFrequencyVisitor<Long>()

        epa.acceptDepthFirst(frequencyVisitor)

        val actual =
            epa.states.joinToString("\n") { state ->
                "$state: ${frequencyVisitor.frequencyByState(state)}"
            }

        expectSelfie(actual).toMatchDisk()
    }
}
