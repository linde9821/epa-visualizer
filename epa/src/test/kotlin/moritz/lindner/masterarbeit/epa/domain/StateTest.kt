package moritz.lindner.masterarbeit.epa.domain

import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class StateTest {
    @Test
    fun `all states must be descendant of root`() {
        val epa =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        assertThat(epa.states).allMatch { state ->
            when (state) {
                is State.PrefixState -> state.isDescendantOf(State.Root)
                State.Root -> true
            }
        }
    }

    @Test
    fun `a b c is not a descendant of a c`() {
        val epa =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val abc =
            epa.states.find { state ->
                state.toString() == "root -> a -> b -> c"
            }!! as State.PrefixState

        val ac =
            epa.states.find { state ->
                state.toString() == "root -> a -> c"
            }!!as State.PrefixState

        assertThat(abc.isDescendantOf(ac)).isFalse
    }

    @Test
    fun `a c b d e is a descendant of a c`() {
        val epa =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val abc =
            epa.states.find { state ->
                state.toString() == "root -> a -> c -> b -> d -> e"
            }!! as State.PrefixState

        val ac =
            epa.states.find { state ->
                state.toString() == "root -> a -> c"
            }!!as State.PrefixState

        assertThat(abc.isDescendantOf(ac)).isTrue
    }
}
