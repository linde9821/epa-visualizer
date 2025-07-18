package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomatonBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.filter.StateFrequencyFilter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class StateFrequencyFilterTest {
    @Test
    fun `must remove all states where the frequency is below the threshold`() {
        val epa =
            ExtendedPrefixAutomatonBuilder<Long>()
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
