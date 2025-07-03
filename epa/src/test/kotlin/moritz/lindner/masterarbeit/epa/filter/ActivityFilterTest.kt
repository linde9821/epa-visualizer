package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.features.filter.ActivityFilter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class ActivityFilterTest {
    @Test
    fun `must return a epa containing only allowed activities`() {
        val epa =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(File("./src/test/resources/filter_sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val allowedActivities =
            hashSetOf(
                Activity("a"),
                Activity("b"),
                Activity("c"),
            )
        val sut =
            ActivityFilter<Long>(
                allowedActivities,
            )

        val result = sut.apply(epa)

        assertThat(result.activities).containsExactlyInAnyOrder(*(allowedActivities.toList()).toTypedArray())
        assertThat(result.transitions).hasSize(3) // included +1 for root
        assertThat(result.states).hasSize(4) // included +1 for root
    }

    @Test
    fun `must return a epa containing only allowed activities and prune orphans`() {
        val epa =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(File("./src/test/resources/filter_sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val allowedActivities =
            hashSetOf(
                Activity("a"),
                Activity("c"),
                Activity("d"),
            )
        val sut =
            ActivityFilter<Long>(
                allowedActivities,
            )

        val result = sut.apply(epa)

        assertThat(result.activities).containsExactlyInAnyOrder(*(allowedActivities.toList()).toTypedArray())
        assertThat(result.transitions).hasSize(2) // included +1 for root
        assertThat(result.states).hasSize(3) // included +1 for root
    }

    @Test
    fun `must return a epa containing only allowed activities and prune orphans even  if they are inside the chain`() {
        val epa =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(File("./src/test/resources/sample.xes"))
                .setEventLogMapper(SampleEventMapper())
                .build()

        val allowedActivities =
            hashSetOf(
                Activity("b"),
                Activity("c"),
                Activity("d"),
                Activity("e"),
                Activity("f"),
            )
        val sut =
            ActivityFilter<Long>(
                allowedActivities,
            )

        val result = sut.apply(epa)

        assertThat(result.activities).containsExactlyInAnyOrder(*(allowedActivities.toList()).toTypedArray())
        assertThat(result.states).hasSize(1) // included +1 for root
    }
}
