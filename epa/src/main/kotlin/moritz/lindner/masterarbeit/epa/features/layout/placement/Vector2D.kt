package moritz.lindner.masterarbeit.epa.features.layout.placement

import moritz.lindner.masterarbeit.epa.domain.State
import kotlin.math.sqrt

data class Vector2D(val x: Float, val y: Float) {
    fun magnitude(): Float = sqrt(x * x + y * y)

    fun normalize(): Vector2D {
        val mag = magnitude()
        return if (mag > 0) {
            Vector2D(x / mag, y / mag)
        } else this
    }

    fun add(other: Vector2D): Vector2D {
        return Vector2D(x + other.x, y + other.y)
    }

    fun multiply(scalar: Float): Vector2D {
        return Vector2D(x * scalar, y * scalar)
    }

    companion object {
        fun zero() = Vector2D(0f, 0f)
    }
}