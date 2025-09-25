package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CombinedFilterTest {

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