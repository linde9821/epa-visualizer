package moritz.lindner.masterarbeit.epa.visitor.case

import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class CaseAnimationTest {
    @Test
    fun `must be able to get previous, current and upcoming state from a key`() {
        val builder = ExtendedPrefixAutomataBuilder<Long>()
        builder.setFile(File("./src/test/resources/sample.xes"))
        builder.setEventLogMapper(SampleEventMapper())
        val epa = builder.build()
        val visitor = CaseAnimationVisitor<Long>("1")

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
