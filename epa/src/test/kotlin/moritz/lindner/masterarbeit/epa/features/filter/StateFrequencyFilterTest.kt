package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class StateFrequencyFilterTest {
    @Test
    fun `must remove all states where the frequency is below the threshold`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/filter_sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = StateFrequencyFilter<Long>(0.3f)

        val result = sut.apply(epa)

        assertThat(result.states).hasSize(2)
        assertThat(result.transitions).hasSize(1)
        assertThat(result.activities).hasSize(1)
    }
}
