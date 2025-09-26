package moritz.lindner.masterarbeit.epa.construction.builder

import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.File

class EpaFromComponentsBuilderTest {
    @Test
    fun `creating a epa from a existing one creates the same epa`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = EpaFromComponentsBuilder<Long>()
            .fromExisting(epa)

        val actual = sut.build()
        assertThat(actual.states).containsExactlyInAnyOrder(*(epa.states.toList().toTypedArray()))
        assertThat(actual.transitions).containsExactlyInAnyOrder(*(epa.transitions.toList().toTypedArray()))
        assertThat(actual.activities).containsExactlyInAnyOrder(*(epa.activities.toList().toTypedArray()))
    }

    @Test
    fun `fails when no states are set`() {
        val epa =
            EpaFromXesBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = EpaFromComponentsBuilder<Long>()
            .fromExisting(epa)
            .setStates(emptySet())

        assertThatThrownBy {
            sut.build()
        }
    }
}