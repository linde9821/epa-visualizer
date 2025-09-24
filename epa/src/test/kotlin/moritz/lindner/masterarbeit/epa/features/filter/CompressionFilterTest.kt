package moritz.lindner.masterarbeit.epa.features.filter

import com.diffplug.selfie.Selfie.expectSelfie
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomatonBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CompressionFilterTest {

    @Test
    fun `must apply filter as expected`() {
        val epa =
            ExtendedPrefixAutomatonBuilder<Long>()
                .setFile(File("./src/test/resources/simple2.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = CompressionFilter<Long>()
        val result = sut.apply(epa)

        assertThat(result.activities.size < epa.activities.size).isTrue
        assertThat(result.states.size < epa.states.size).isTrue
        assertThat(result.transitions.size < epa.transitions.size).isTrue
        assertThat(result.states.map { result.sequence(it) }.flatten()).containsAll(epa.states.map { epa.sequence(it) }
            .flatten())

        expectSelfie(result.toString()).toMatchDisk()
    }
}