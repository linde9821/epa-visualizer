package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.features.layout.ColorPalettes
import moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.semantic.LogarithmicCycleTime
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import kotlin.math.log10

class DefaultConfig(
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val stateSize: Float,
    private val minTransitionSize: Float,
    private val maxTransitionSize: Float,
    private val progressCallback: EpaProgressCallback? = null,
    private val colorPalette: String
) : AtlasConfig {

    private val epaService = EpaService<Long>()

    val logarithmicNormalizedCycleTimeByState = LogarithmicCycleTime.logarithmicMinMaxNormalizedCycleTimeByState(
        extendedPrefixAutomaton = extendedPrefixAutomaton,
        min = 0.0f,
        max = 1.0f,
        progressCallback = progressCallback
    )

    private val normalizedStateFrequency = epaService
        .getNormalizedStateFrequency(
            epa = extendedPrefixAutomaton,
            progressCallback = progressCallback
        )

    override fun toStateAtlasEntry(state: State): StateAtlasEntry {
        return StateAtlasEntry(
            size = stateSize,
            paint = toHeatmapPaint(state)
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
        state: State
    ): Paint {
        val value = logarithmicNormalizedCycleTimeByState[state]!!

        val heatmap = ColorPalettes.colorPalette(colorPalette).map { rgb ->
            Color.makeRGB((rgb shr 16) and 0xFF, (rgb shr 8) and 0xFF, rgb and 0xFF)
        }.toIntArray()

        val colorPositions = FloatArray(heatmap.size) { i ->
            i.toFloat() / (heatmap.size - 1)
        }

        for (i in 0 until colorPositions.size - 1) {
            if (value >= colorPositions[i] && value <= colorPositions[i + 1]) {
                val range = colorPositions[i + 1] - colorPositions[i]
                val factor = (value - colorPositions[i]) / range

                val color = interpolateColor(heatmap[i], heatmap[i + 1], factor)
                return Paint().apply {
                    this.color = color
                }
            }
        }

        val color = heatmap.last()
        return Paint().apply {
            this.color = color
        }
    }

    /** Linearly interpolates between two colors */
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