package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode

class DefaultConfig(
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val stateSize: Float,
    private val minTransitionSize: Float,
    private val maxTransitionSize: Float,
    private val progressCallback: EpaProgressCallback? = null
) : AtlasConfig {

    private val epaService = EpaService<Long>()
    private val cycleTimeByState = epaService.computeAllCycleTimes(
        extendedPrefixAutomaton = extendedPrefixAutomaton,
        minus = Long::minus,
        average = { cycleTimes ->
            if (cycleTimes.isEmpty()) {
                0f
            } else cycleTimes.average().toFloat()
        },
        progressCallback = progressCallback
    )
    private val minCycleTime = cycleTimeByState.values.min()
    private val maxCycleTime = cycleTimeByState.values.max()

    private val normalizedStateFrequency = epaService
        .getNormalizedStateFrequency(
            epa = extendedPrefixAutomaton,
            progressCallback = progressCallback
        )

    override fun toStateAtlasEntry(state: State): StateAtlasEntry {
        return StateAtlasEntry(
            size = stateSize,
            paint = toHeatmapPaint(
                value = cycleTimeByState[state]!!,
                min = minCycleTime,
                max = maxCycleTime
            )
        )
    }

    override fun toTransitionAtlasEntry(transition: Transition): TransitionAtlasEntry {
        val freq = normalizedStateFrequency.frequencyByState(transition.end)

        val width = linearProjectClamped(
            freq,
            normalizedStateFrequency.min(),
            normalizedStateFrequency.max(),
            minTransitionSize,
            maxTransitionSize
        )

        return TransitionAtlasEntry(
            paint = Paint().apply {
                color = Color.BLACK
                mode = PaintMode.STROKE
                strokeWidth = width
                isAntiAlias = true
            }
        )
    }

    fun linearProjectClamped(
        value: Float,
        oldMin: Float,
        oldMax: Float,
        newMin: Float,
        newMax: Float
    ): Float {
        val projected = newMin + (value - oldMin) * (newMax - newMin) / (oldMax - oldMin)
        return projected.coerceIn(newMin, newMax)
    }

    fun toHeatmapPaint(
        value: Float,
        min: Float,
        max: Float
    ): Paint {
        val clampedValue = ((value - min) / (max - min)).coerceIn(0.0f, 1.0f)

        val heatmapColors = intArrayOf(
            Color.makeRGB(0, 0, 139),      // Dark blue (coldest)
            Color.makeRGB(0, 0, 255),      // Blue
            Color.makeRGB(0, 255, 255),    // Cyan
            Color.makeRGB(0, 255, 0),      // Green
            Color.makeRGB(255, 255, 0),    // Yellow
            Color.makeRGB(255, 165, 0),    // Orange
            Color.makeRGB(255, 0, 0),      // Red (hottest)
        )

        val colorPositions = floatArrayOf(
            0.0f,   // Dark blue at 0%
            0.17f,  // Blue at 17%
            0.33f,  // Cyan at 33%
            0.5f,   // Green at 50%
            0.67f,  // Yellow at 67%
            0.83f,  // Orange at 83%
            1.0f    // Red at 100%
        )

        for (i in 0 until colorPositions.size - 1) {
            if (clampedValue >= colorPositions[i] && clampedValue <= colorPositions[i + 1]) {
                // Calculate interpolation factor between the two colors
                val range = colorPositions[i + 1] - colorPositions[i]
                val factor = (clampedValue - colorPositions[i]) / range

                // Interpolate between the two colors
                val color = interpolateColor(heatmapColors[i], heatmapColors[i + 1], factor)
                return Paint().apply {
                    this.color = color
                }
            }
        }

        val color = heatmapColors.last()
        return Paint().apply {
            this.color = color
        }
    }

    /**
     * Linearly interpolates between two colors
     */
    private fun interpolateColor(color1: Int, color2: Int, factor: Float): Int {
        val r1 = Color.getR(color1)
        val g1 = Color.getG(color1)
        val b1 = Color.getB(color1)
        val a1 = Color.getA(color1)

        val r2 = Color.getR(color2)
        val g2 = Color.getG(color2)
        val b2 = Color.getB(color2)
        val a2 = Color.getA(color2)

        val r = (r1 + (r2 - r1) * factor).toInt()
        val g = (g1 + (g2 - g1) * factor).toInt()
        val b = (b1 + (b2 - b1) * factor).toInt()
        val a = (a1 + (a2 - a1) * factor).toInt()

        return Color.makeARGB(a, r, g, b)
    }
}