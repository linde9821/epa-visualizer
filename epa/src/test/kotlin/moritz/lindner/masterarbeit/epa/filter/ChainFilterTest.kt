package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.domain.State
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class ChainFilterTest {
    @Test
    fun `must contain no states with 1 or less children`() {
        val epa =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = ChainFilter<Long>()
        val newEpa = sut.apply(epa)

        assertThat(newEpa.states).allMatch { state ->
            epa.outgoingTransitionsByState[state]!!.size > 1
        }
    }

    @Test
    fun `must combine chain into new states`() {
        val epa =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val sut = ChainFilter<Long>()
        val newEpa = sut.apply(epa)

        assertThat(newEpa.states).allMatch {
            epa.states.contains(it)
        }

        val expectedPresent =
            epa.states.find { state ->
                state.toString() == "root -> a"
            } as State.PrefixState?

        val expectedNotPresent =
            epa.states.find { state ->
                state.toString() == "root -> a -> c"
            } as State.PrefixState?

        assertThat(expectedPresent).isNotNull
        assertThat(expectedNotPresent).isNotNull
    }
}
