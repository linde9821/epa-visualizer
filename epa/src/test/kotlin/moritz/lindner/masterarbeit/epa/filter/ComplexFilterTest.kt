package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomatonBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.features.filter.ActivityFilter
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.filter.PartitionFrequencyFilter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class ComplexFilterTest {
    @Test
    fun `must apply combined filter correctly`() {
        val epa =
            ExtendedPrefixAutomatonBuilder<Long>()
                .setFile(File("./src/test/resources/filter_sample_complex.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val filter1 = PartitionFrequencyFilter<Long>(0.07f) // must remove partition ending with d
        val filter2 =
            ActivityFilter<Long>(
                hashSetOf(
                    Activity("a"),
                    Activity("b"),
                    Activity("d"),
                ),
            ) // must remove partition ending with c

        val sut = EpaFilter.combine(listOf(filter1, filter2))

        val result = sut.apply(epa)

        assertThat(result.states).hasSize(3)
        assertThat(result.transitions).hasSize(2)
        assertThat(result.activities).hasSize(2)
    }
}
