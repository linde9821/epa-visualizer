package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint

data class StateAtlasEntry(
    val size: Float,
    val paint: Paint,
)

data class TransitionAtlasEntry(
    val startWidth: Float,
    val endWith: Float,
    val paint: Paint,
)

interface StateAtlasConfig {
    fun toPaint(state: State): Paint
    fun toSize(state: State): Float
}

class LinearConfig(
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val sizeMin: Float,
    private val sizeMax: Float,
): StateAtlasConfig {

    private val epaService = EpaService<Long>()

    private val cycleTimeByState = extendedPrefixAutomaton.states.associateWith { state ->
        epaService.computeCycleTime(
            extendedPrefixAutomaton = extendedPrefixAutomaton,
            state = state,
            minus = Long::minus,
            addition = Long::plus,
            average = { cycleTimes ->
                cycleTimes.average().toFloat()
            }
        )
    }

    private val minCycleTime = cycleTimeByState.values.min()
    private val maxCycleTime = cycleTimeByState.values.max()

    val normalizedStateFrequency = epaService.getNormalizedStateFrequency(extendedPrefixAutomaton)

    override fun toPaint(state: State): Paint {
        return mapToRedGreen(
            normalizedStateFrequency.frequencyByState(state),
            normalizedStateFrequency.min(),
            normalizedStateFrequency.max()
        )
    }

    override fun toSize(state: State): Float {
        val cycleTime = cycleTimeByState[state]!!
        return linearProjectClamped(
            cycleTime,
            minCycleTime,
            maxCycleTime,
            sizeMin,
            sizeMax
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

    fun mapToRedGreen(
        value: Float,
        min: Float,
        max: Float
    ): Paint {
        val normalized = ((value - min) / (max - min)).coerceIn(0.0, 1.0)

        val red = ((1 - normalized) * 255).toInt()
        val green = (normalized * 255).toInt()

        val color = Color.makeRGB(red, green, 0)

        return Paint().apply {
            this.color = color
        }
    }
}

class DrawingAtlas {
    val atlasByState = HashMap<State, StateAtlasEntry>()
//    val transitionByState = HashMap<State, TransitionAtlasEntry>()

    fun add(state: State, entry: StateAtlasEntry) {
        atlasByState[state] = entry
    }

    companion object {
        fun <T : Comparable<T>> build(
            extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
            stateAtlasConfig: StateAtlasConfig
        ): DrawingAtlas {
            val atlas = DrawingAtlas()

            extendedPrefixAutomaton.states.forEach { state ->
                val paint = stateAtlasConfig.toPaint(state)
                val size = stateAtlasConfig.toSize(state)

                atlas.add(state, StateAtlasEntry(size, paint))
            }

            return atlas
        }
    }
}