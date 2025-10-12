package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint

class LinearSizeByCycleTimeAndLinearColorByFrequencyAtlasConfig(
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val minSize: Float,
    private val maxSize: Float,
) : StateAtlasConfig {

    private val epaService = EpaService<Long>()

    private val cycleTimeByState = epaService.computeAllCycleTimes(
        extendedPrefixAutomaton = extendedPrefixAutomaton,
        minus = Long::minus,
        average = { cycleTimes ->
            if (cycleTimes.isEmpty()) {
                0f
            } else cycleTimes.average().toFloat()
        }
    )
    private val minCycleTime = cycleTimeByState.values.min()
    private val maxCycleTime = cycleTimeByState.values.max()

    private val normalizedStateFrequency = epaService.getNormalizedStateFrequency(extendedPrefixAutomaton)

    override fun toPaint(state: State): Paint {
        return mapToRedGreen(
            normalizedStateFrequency.frequencyByState(state),
            normalizedStateFrequency.min(),
            normalizedStateFrequency.max()
        )
    }

    override fun toSize(state: State): Float {
        val cycleTime = cycleTimeByState[state]!!

        if (minCycleTime == maxCycleTime) {
            return (minSize + maxSize) / 2
        }

        return maxSize

//        return linearProjectClamped(
//            cycleTime,
//            minCycleTime,
//            maxCycleTime,
//            sizeMin,
//            sizeMax
//        )
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

    fun mapToRedGreen(
        value: Float,
        min: Float,
        max: Float
    ): Paint {
        val normalized = ((value - min) / (max - min)).coerceIn(0.0f, 1.0f)

        val red = ((1 - normalized) * 255).toInt()
        val green = (normalized * 255).toInt()

        val color = Color.makeRGB(red, green, 0)

        return Paint().apply {
            this.color = color
        }
    }
}