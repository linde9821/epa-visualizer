package moritz.lindner.masterarbeit.epa.visitor.case

import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CaseAnimationTest {
    @Test
    fun `getNthEntry returns the correct timestamp and state`() {
        val builder = ExtendedPrefixAutomataBuilder<Long>()
        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())
        val epa = builder.build()
        val visitor = SingleCaseAnimationVisitor<Long>("1")

        epa.acceptDepthFirst(visitor)

        val sut = visitor.build()

        val (timestamp, state) = sut.getNthEntry(0)!!

        assertThat(timestamp).isNotNull()
        assertThat(state).isNotNull()
        assertThat(state.name).isEqualTo("a")
    }

    @Test
    fun `getStateUpTillTimestamp and getStateFromTimestamp split timeline correctly`() {
        val builder = ExtendedPrefixAutomataBuilder<Long>()
        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())
        val epa = builder.build()
        val visitor = SingleCaseAnimationVisitor<Long>("1")

        epa.acceptDepthFirst(visitor)

        val sut = visitor.build()

        val (timestamp, state) = sut.getNthEntry(2)!!
        val previous = sut.getStateUpTillTimestamp(timestamp)
        val upcoming = sut.getStateFromTimestamp(timestamp)

        assertThat(previous).doesNotContain(state)
        assertThat(upcoming).doesNotContain(state)
        assertThat(previous).doesNotContainAnyElementsOf(upcoming)
        assertThat(previous.size + upcoming.size + 1).isEqualTo(sut.totalAmountOfEvents)
    }

    @Test
    fun `must be able to get previous, current and upcoming state from a key`() {
        val builder = ExtendedPrefixAutomataBuilder<Long>()
        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())
        val epa = builder.build()
        val visitor = SingleCaseAnimationVisitor<Long>("1")

        epa.acceptDepthFirst(visitor)

        val sut = visitor.build()

        val (key, current) = sut.getNthEntry(3)!!
        val next = sut.getStateFromTimestamp(key)
        val previous = sut.getStateUpTillTimestamp(key)

        assertThat(next).doesNotContain(current)
        assertThat(next).hasSize(2)
        assertThat(previous).doesNotContain(current)
        assertThat(previous).hasSize(3)
        assertThat(previous).doesNotContainAnyElementsOf(next)
        assertThat(next.size + previous.size + 1).isEqualTo(6)
    }
}
