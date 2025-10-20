package moritz.lindner.masterarbeit.epa.features.layout.placement

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Rectangle")
class RectangleTest {

    @Nested
    @DisplayName("Creation")
    inner class Creation {
        @Test
        fun `should create rectangle with correct properties`() {
            val topLeft = Coordinate(0f, 0f)
            val bottomRight = Coordinate(10f, 10f)
            val rectangle = Rectangle(topLeft, bottomRight)

            assertThat(rectangle.topLeft).isEqualTo(topLeft)
            assertThat(rectangle.bottomRight).isEqualTo(bottomRight)
        }

        @Test
        fun `should handle negative coordinates`() {
            val rectangle = Rectangle(
                topLeft = Coordinate(-10f, -10f),
                bottomRight = Coordinate(-5f, -5f)
            )

            assertThat(rectangle.topLeft.x).isEqualTo(-10f)
            assertThat(rectangle.bottomRight.x).isEqualTo(-5f)
        }
    }

    @Nested
    @DisplayName("contains() - standard Cartesian coordinates")
    inner class ContainsStandard {
        private val rectangle = Rectangle(
            topLeft = Coordinate(0f, 0f),
            bottomRight = Coordinate(10f, 10f)
        )

        @Test
        fun `should contain point at top-left corner`() {
            assertThat(rectangle.contains(Coordinate(0f, 0f))).isTrue()
        }

        @Test
        fun `should contain point at bottom-right corner`() {
            assertThat(rectangle.contains(Coordinate(10f, 10f))).isTrue()
        }

        @Test
        fun `should contain point at center`() {
            assertThat(rectangle.contains(Coordinate(5f, 5f))).isTrue()
        }

        @Test
        fun `should contain points on edges`() {
            assertThat(rectangle.contains(Coordinate(5f, 0f))).isTrue()  // top edge
            assertThat(rectangle.contains(Coordinate(5f, 10f))).isTrue() // bottom edge
            assertThat(rectangle.contains(Coordinate(0f, 5f))).isTrue()  // left edge
            assertThat(rectangle.contains(Coordinate(10f, 5f))).isTrue() // right edge
        }

        @Test
        fun `should not contain point outside left boundary`() {
            assertThat(rectangle.contains(Coordinate(-1f, 5f))).isFalse()
        }

        @Test
        fun `should not contain point outside right boundary`() {
            assertThat(rectangle.contains(Coordinate(11f, 5f))).isFalse()
        }

        @Test
        fun `should not contain point outside top boundary`() {
            assertThat(rectangle.contains(Coordinate(5f, -1f))).isFalse()
        }

        @Test
        fun `should not contain point outside bottom boundary`() {
            assertThat(rectangle.contains(Coordinate(5f, 11f))).isFalse()
        }

        @Test
        fun `should not contain point in top-left quadrant outside rectangle`() {
            assertThat(rectangle.contains(Coordinate(-5f, -5f))).isFalse()
        }

        @Test
        fun `should not contain point in bottom-right quadrant outside rectangle`() {
            assertThat(rectangle.contains(Coordinate(15f, 15f))).isFalse()
        }
    }

    @Nested
    @DisplayName("contains() - swapped coordinates (robustness)")
    inner class ContainsSwapped {
        @Test
        fun `should handle swapped x coordinates correctly`() {
            val rectangle = Rectangle(
                topLeft = Coordinate(10f, 0f),  // x values swapped
                bottomRight = Coordinate(0f, 10f)
            )

            assertThat(rectangle.contains(Coordinate(5f, 5f))).isTrue()
            assertThat(rectangle.contains(Coordinate(-1f, 5f))).isFalse()
            assertThat(rectangle.contains(Coordinate(11f, 5f))).isFalse()
        }

        @Test
        fun `should handle swapped y coordinates correctly`() {
            val rectangle = Rectangle(
                topLeft = Coordinate(0f, 10f),  // y values swapped
                bottomRight = Coordinate(10f, 0f)
            )

            assertThat(rectangle.contains(Coordinate(5f, 5f))).isTrue()
            assertThat(rectangle.contains(Coordinate(5f, -1f))).isFalse()
            assertThat(rectangle.contains(Coordinate(5f, 11f))).isFalse()
        }

        @Test
        fun `should handle both x and y coordinates swapped`() {
            val rectangle = Rectangle(
                topLeft = Coordinate(10f, 10f),
                bottomRight = Coordinate(0f, 0f)
            )

            assertThat(rectangle.contains(Coordinate(5f, 5f))).isTrue()
            assertThat(rectangle.contains(Coordinate(-1f, 5f))).isFalse()
            assertThat(rectangle.contains(Coordinate(11f, 5f))).isFalse()
        }
    }

    @Nested
    @DisplayName("contains() - negative coordinate rectangles")
    inner class ContainsNegative {
        private val rectangle = Rectangle(
            topLeft = Coordinate(-10f, -10f),
            bottomRight = Coordinate(-5f, -5f)
        )

        @Test
        fun `should contain point inside negative coordinate rectangle`() {
            assertThat(rectangle.contains(Coordinate(-7f, -7f))).isTrue()
        }

        @Test
        fun `should contain point at corners of negative rectangle`() {
            assertThat(rectangle.contains(Coordinate(-10f, -10f))).isTrue()
            assertThat(rectangle.contains(Coordinate(-5f, -5f))).isTrue()
        }

        @Test
        fun `should not contain point outside negative rectangle`() {
            assertThat(rectangle.contains(Coordinate(-11f, -7f))).isFalse()
            assertThat(rectangle.contains(Coordinate(-4f, -7f))).isFalse()
        }
    }

    @Nested
    @DisplayName("contains() - rectangles spanning origin")
    inner class ContainsSpanningOrigin {
        private val rectangle = Rectangle(
            topLeft = Coordinate(-5f, -5f),
            bottomRight = Coordinate(5f, 5f)
        )

        @Test
        fun `should contain origin point`() {
            assertThat(rectangle.contains(Coordinate(0f, 0f))).isTrue()
        }

        @Test
        fun `should contain points in all quadrants`() {
            assertThat(rectangle.contains(Coordinate(-3f, -3f))).isTrue()
            assertThat(rectangle.contains(Coordinate(3f, -3f))).isTrue()
            assertThat(rectangle.contains(Coordinate(-3f, 3f))).isTrue()
            assertThat(rectangle.contains(Coordinate(3f, 3f))).isTrue()
        }

        @Test
        fun `should not contain points outside spanning rectangle`() {
            assertThat(rectangle.contains(Coordinate(-6f, 0f))).isFalse()
            assertThat(rectangle.contains(Coordinate(6f, 0f))).isFalse()
            assertThat(rectangle.contains(Coordinate(0f, -6f))).isFalse()
            assertThat(rectangle.contains(Coordinate(0f, 6f))).isFalse()
        }
    }

    @Nested
    @DisplayName("contains() - edge cases")
    inner class ContainsEdgeCases {
        @Test
        fun `should handle zero-width rectangle`() {
            val rectangle = Rectangle(
                topLeft = Coordinate(5f, 0f),
                bottomRight = Coordinate(5f, 10f)
            )

            assertThat(rectangle.contains(Coordinate(5f, 5f))).isTrue()
            assertThat(rectangle.contains(Coordinate(4.9f, 5f))).isFalse()
            assertThat(rectangle.contains(Coordinate(5.1f, 5f))).isFalse()
        }

        @Test
        fun `should handle zero-height rectangle`() {
            val rectangle = Rectangle(
                topLeft = Coordinate(0f, 5f),
                bottomRight = Coordinate(10f, 5f)
            )

            assertThat(rectangle.contains(Coordinate(5f, 5f))).isTrue()
            assertThat(rectangle.contains(Coordinate(5f, 4.9f))).isFalse()
            assertThat(rectangle.contains(Coordinate(5f, 5.1f))).isFalse()
        }

        @Test
        fun `should handle point rectangle (zero area)`() {
            val rectangle = Rectangle(
                topLeft = Coordinate(5f, 5f),
                bottomRight = Coordinate(5f, 5f)
            )

            assertThat(rectangle.contains(Coordinate(5f, 5f))).isTrue()
            assertThat(rectangle.contains(Coordinate(5.1f, 5f))).isFalse()
            assertThat(rectangle.contains(Coordinate(5f, 5.1f))).isFalse()
        }
    }

    @Nested
    @DisplayName("contains() - floating point precision")
    inner class ContainsFloatingPoint {
        private val rectangle = Rectangle(
            topLeft = Coordinate(0f, 0f),
            bottomRight = Coordinate(10f, 10f)
        )

        @Test
        fun `should handle very small coordinates near boundary`() {
            assertThat(rectangle.contains(Coordinate(0.0001f, 0.0001f))).isTrue()
            assertThat(rectangle.contains(Coordinate(9.9999f, 9.9999f))).isTrue()
        }

        @Test
        fun `should handle coordinates just outside boundary`() {
            assertThat(rectangle.contains(Coordinate(-0.0001f, 5f))).isFalse()
            assertThat(rectangle.contains(Coordinate(10.0001f, 5f))).isFalse()
        }
    }

    @Nested
    @DisplayName("Data class behavior")
    inner class DataClassBehavior {
        @Test
        fun `should support equality`() {
            val rect1 = Rectangle(Coordinate(0f, 0f), Coordinate(10f, 10f))
            val rect2 = Rectangle(Coordinate(0f, 0f), Coordinate(10f, 10f))
            val rect3 = Rectangle(Coordinate(0f, 0f), Coordinate(5f, 5f))

            assertThat(rect1).isEqualTo(rect2)
            assertThat(rect1).isNotEqualTo(rect3)
        }

        @Test
        fun `should support copy`() {
            val original = Rectangle(Coordinate(0f, 0f), Coordinate(10f, 10f))
            val copied = original.copy(bottomRight = Coordinate(20f, 20f))

            assertThat(copied.topLeft).isEqualTo(original.topLeft)
            assertThat(copied.bottomRight).isEqualTo(Coordinate(20f, 20f))
        }

        @Test
        fun `should support destructuring`() {
            val rectangle = Rectangle(Coordinate(1f, 2f), Coordinate(3f, 4f))
            val (topLeft, bottomRight) = rectangle

            assertThat(topLeft).isEqualTo(Coordinate(1f, 2f))
            assertThat(bottomRight).isEqualTo(Coordinate(3f, 4f))
        }

        @Test
        fun `should have proper toString representation`() {
            val rectangle = Rectangle(Coordinate(1f, 2f), Coordinate(3f, 4f))

            assertThat(rectangle.toString())
                .contains("topLeft")
                .contains("bottomRight")
        }

        @Test
        fun `should have proper hashCode`() {
            val rect1 = Rectangle(Coordinate(0f, 0f), Coordinate(10f, 10f))
            val rect2 = Rectangle(Coordinate(0f, 0f), Coordinate(10f, 10f))

            assertThat(rect1.hashCode()).isEqualTo(rect2.hashCode())
        }
    }
}