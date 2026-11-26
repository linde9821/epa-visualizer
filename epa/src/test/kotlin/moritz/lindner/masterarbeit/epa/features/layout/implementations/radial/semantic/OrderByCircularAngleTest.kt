package moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.semantic

import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.State
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("orderByCircularAngle")
class OrderByCircularAngleTest {

    private fun child(name: String): State = State.PrefixState(State.Root, Activity(name))

    private fun order(angles: Map<State, Float>, sizes: Map<State, Int> = emptyMap()): List<String> =
        orderByCircularAngle(
            items = angles.keys.toList(),
            angleOf = { angles[it]!! },
            sizeOf = { sizes[it] ?: 0 }
        ).map { it.name }

    @Nested
    @DisplayName("Plain ordering")
    inner class PlainOrdering {
        @Test
        fun `should keep empty and single item lists unchanged`() {
            assertThat(order(emptyMap())).isEmpty()
            assertThat(order(mapOf(child("a") to 3f))).containsExactly("a")
        }

        @Test
        fun `should order by ascending angle when largest gap is the wrap-around gap`() {
            val angles = mapOf(
                child("c") to 2.0f,
                child("a") to 1.0f,
                child("b") to 1.5f,
            )

            assertThat(order(angles)).containsExactly("a", "b", "c")
        }

        @Test
        fun `should ignore subtree size when angles differ`() {
            val a = child("a")
            val b = child("b")
            val c = child("c")
            val angles = mapOf(c to 2.0f, a to 1.0f, b to 1.5f)
            val sizes = mapOf(a to 1, b to 100, c to 50)

            assertThat(order(angles, sizes)).containsExactly("a", "b", "c")
        }

        @Test
        fun `should cut at the largest interior gap`() {
            // gaps: a->b 0.2, b->c 3.0, c->d 0.2, wrap d->a ~2.88
            val angles = mapOf(
                child("a") to 0.0f,
                child("b") to 0.2f,
                child("c") to 3.2f,
                child("d") to 3.4f,
            )

            assertThat(order(angles)).containsExactly("c", "d", "a", "b")
        }
    }

    @Nested
    @DisplayName("Wrap-around")
    inner class WrapAround {
        @Test
        fun `should keep angles near 0 and near 2PI adjacent`() {
            val angles = mapOf(
                child("low") to 0.1f,
                child("high") to 6.2f,
                child("mid") to 3.0f,
            )

            // gaps: low->mid 2.9, mid->high 3.2 (largest), wrap high->low ~0.18
            assertThat(order(angles)).containsExactly("high", "low", "mid")
        }

        @Test
        fun `should keep a cluster spanning 0 contiguous`() {
            val angles = mapOf(
                child("a") to 6.0f,
                child("b") to 6.2f,
                child("c") to 0.05f,
                child("d") to 0.3f,
                child("e") to 3.0f,
            )

            val result = order(angles)

            assertThat(result).containsExactly("a", "b", "c", "d", "e")
        }

        @Test
        fun `should normalize angles outside 0 to 2PI`() {
            val angles = mapOf(
                child("neg") to -0.1f, // equals ~6.18
                child("small") to 0.1f,
                child("mid") to 3.0f,
            )

            assertThat(order(angles)).containsExactly("neg", "small", "mid")
        }
    }

    @Nested
    @DisplayName("Ties")
    inner class Ties {
        @Test
        fun `should break equal angles by subtree size descending`() {
            val a = child("a")
            val b = child("b")
            val c = child("c")
            val angles = mapOf(a to 1.0f, b to 1.0f, c to 2.0f)
            val sizes = mapOf(a to 2, b to 10, c to 1)

            assertThat(order(angles, sizes)).containsExactly("b", "a", "c")
        }

        @Test
        fun `should break equal angles and sizes by state name`() {
            val angles = mapOf(
                child("z") to 1.0f,
                child("x") to 1.0f,
                child("y") to 1.0f,
            )

            assertThat(order(angles)).containsExactly("x", "y", "z")
        }

        @Test
        fun `should be independent of input order`() {
            val states = listOf("d", "a", "c", "b").map(::child)
            val angles = mapOf(states[0] to 5.0f, states[1] to 1.0f, states[2] to 1.0f, states[3] to 6.0f)

            val forward = orderByCircularAngle(states, { angles[it]!! }, { 0 }).map { it.name }
            val backward = orderByCircularAngle(states.reversed(), { angles[it]!! }, { 0 }).map { it.name }

            assertThat(forward).isEqualTo(backward)
            assertThat(forward).containsExactly("d", "b", "a", "c")
        }
    }
}
