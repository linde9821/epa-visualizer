package moritz.lindner.masterarbeit.epa.features.cycletime

import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.domain.Activity
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

        val sut = CycleTimes<Long>()

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

        val sut = CycleTimes<Long>()

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

        val sut = CycleTimes<Long>()

        epa.acceptDepthFirst(sut)

        val state = epa.states.first { it.name == "b" && (it as? State.PrefixState)?.from?.name == "a" }
        val event = epa.sequence(state).first()

        val next = sut.getNextEventInTraceAtDifferentState(event, state)
        val ct = (next?.timestamp ?: Long.MAX_VALUE) - event.timestamp

        assertThat(next).isNotNull
        assertThat(ct).isEqualTo(1L * 60 * 1000)
    }

    @Test
    fun `must get specific event at specific state`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = CycleTimes<Long>()

        epa.acceptDepthFirst(sut)

        val state = epa.states.first { it.name == "b" && (it as? State.PrefixState)?.from?.name == "a" }
        val event = epa.sequence(state).first()
        val expectedNext = epa.states.first { it.name == "d" && (it as? State.PrefixState)?.from?.name == "c" }

        val next = sut.getNextEventInTraceAtSpecificState(event, state, expectedNext)

        assertThat(next!!.activity).isEqualTo(Activity("d"))
    }

    @Test
    fun `must not find event if not in children`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = CycleTimes<Long>()

        epa.acceptDepthFirst(sut)

        val state = epa.states.first { it.name == "b" && (it as? State.PrefixState)?.from?.name == "a" }
        val event = epa.sequence(state).first()
        val expectedNext = epa.states.first { it.name == "c" && (it as? State.PrefixState)?.from?.name == "a" }

        val next = sut.getNextEventInTraceAtSpecificState(event, state, expectedNext)

        assertThat(next).isNull()
    }
}