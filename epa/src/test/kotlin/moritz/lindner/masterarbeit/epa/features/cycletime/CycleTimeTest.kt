package moritz.lindner.masterarbeit.epa.features.cycletime

import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.filter.CompressionFilter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CycleTimeTest {
    @Test
    fun `must return next event in trace`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = CycleTime<Long>()

        epa.acceptDepthFirst(sut)

        val state = epa.states.first { it.name == "b" && (it as? State.PrefixState)?.from?.name == "a" }

        val event = epa.sequence(state).first()

        val next = sut.getNextEventInTraceAtDifferentState(event, state)

        assertThat(next).isNotNull()
        assertThat(next?.activity?.name).isEqualTo("c")
    }

    @Test
    fun `must return next event of other state when epa is compressed`() {
        val epa1 =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val filter = CompressionFilter<Long>()
        val epa = filter.apply(epa1)

        val sut = CycleTime<Long>()

        epa.acceptDepthFirst(sut)

        val state = epa.states.first { it.name == "b-c-d" }

        val event = epa.sequence(state).first()

        val next = sut.getNextEventInTraceAtDifferentState(event, state)

        assertThat(next).isNotNull()
        assertThat(next?.activity?.name).isEqualTo("f")
    }

    @Test
    fun `cycle time must be correct value`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = CycleTime<Long>()

        epa.acceptDepthFirst(sut)

        val state = epa.states.first { it.name == "b" && (it as? State.PrefixState)?.from?.name == "a" }
        val event = epa.sequence(state).first()

        val ct = sut.cycleTimeOfEventInTrace(event, state, Long::minus)

        assertThat(ct).isEqualTo(1L * 60 * 1000)
    }
}