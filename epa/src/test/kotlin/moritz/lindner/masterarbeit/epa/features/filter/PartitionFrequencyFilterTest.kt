package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomatonBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class PartitionFrequencyFilterTest {
    @Test
    fun `must remove all partitions where the frequency is below the threshold`() {
        val epa =
            ExtendedPrefixAutomatonBuilder<Long>()
                .setFile(File("./src/test/resources/filter_sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = PartitionFrequencyFilter<Long>(0.95f)

        val result = sut.apply(epa)

        assertThat(result.states).hasSize(1)
        assertThat(result.transitions).hasSize(0)
        assertThat(result.activities).hasSize(0)
    }
}
