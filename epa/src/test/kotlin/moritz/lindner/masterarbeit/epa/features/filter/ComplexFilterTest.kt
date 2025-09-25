package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.domain.Activity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class ComplexFilterTest {
    @Test
    fun `must apply combined filter correctly`() {
        val epa =
            EpaFromXesBuilder<Long>()
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

        val result = filter2.apply(filter1.apply(epa))

        assertThat(result.states).hasSize(3)
        assertThat(result.transitions).hasSize(2)
        assertThat(result.activities).hasSize(2)
    }

    @Test
    fun `applying partition filter and then compression works`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/simple2.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()
        val epaService = EpaService<Long>()

        val partitionFrequencyFilter = PartitionFrequencyFilter<Long>(threshold = 0.5f)
        val compressionFilter = CompressionFilter<Long>()
        val filters = listOf(partitionFrequencyFilter, compressionFilter)

        val onlyPartition = partitionFrequencyFilter.apply(epa)
        val combined = epaService.applyFilters(epa, filters)

        assertThat(combined.getAllPartitions().size).isEqualTo(onlyPartition.getAllPartitions().size)
    }
}
