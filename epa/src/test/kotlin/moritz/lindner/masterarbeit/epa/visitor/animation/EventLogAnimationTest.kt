package moritz.lindner.masterarbeit.epa.visitor.animation

import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomatonBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.features.animation.EventLogAnimation
import moritz.lindner.masterarbeit.epa.features.animation.SingleCaseAnimationBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class EventLogAnimationTest {
    private fun buildSingleCaseAnimation(): EventLogAnimation<Long> {
        val builder = ExtendedPrefixAutomatonBuilder<Long>()
        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())
        val epa = builder.build()
        val visitor = SingleCaseAnimationBuilder<Long>("1")
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    @Test
    fun `getNthEntry returns the correct timestamp and state`() {
        val sut = buildSingleCaseAnimation()

        val (timestamp, state) = sut.getNthEntry(0)!!

        assertThat(timestamp).isNotNull()
        assertThat(state).isNotNull()
        assertThat(state.state.name).isEqualTo("a")
    }

    @Test
    fun `getActiveStatesAt returns expected state around the timestamp`() {
        val sut = buildSingleCaseAnimation()

        val (timestamp, state) = sut.getNthEntry(2)!!

        val current = sut.getActiveStatesAt(timestamp)

        assertThat(current).contains(state)
        assertThat(current).anyMatch { it == state }
    }
}
