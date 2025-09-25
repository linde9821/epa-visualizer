package moritz.lindner.masterarbeit.epa.visitor.statistics

import moritz.lindner.masterarbeit.epa.construction.builder.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.statistics.StatisticsVisitor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class StatisticsVisitorTest {
    @Test
    fun `must calculate right amount of paritions`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = StatisticsVisitor<Long>()

        epa.acceptDepthFirst(sut)

        val statistics = sut.build()

        assertThat(statistics.partitionsCount).isEqualTo(4)
    }
}
