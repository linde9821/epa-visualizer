package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import org.junit.jupiter.api.Test
import java.io.File

class FrequencyFilterTest {
    @Test
    fun `must remove all states where the frequency is below the threshold`() {
        val epa =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(File("./src/test/resources/filter_sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = FrequencyFilter<Long>(0.5f)

        val result = sut.apply(epa)

        TODO()
    }
}
