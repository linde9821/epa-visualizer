package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode

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

class LinearStateAtlasConfig(
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val sizeMin: Float,
    private val sizeMax: Float,
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
    private val minCycleTime = cycleTimeByState.filter { it.key != State.Root }.values.min()
    private val maxCycleTime = cycleTimeByState.filter { it.key != State.Root }.values.max()

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
            return (sizeMin + sizeMax) / 2
        }

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
        val normalized = ((value - min) / (max - min)).coerceIn(0.0f, 1.0f)

        val red = ((1 - normalized) * 255).toInt()
        val green = (normalized * 255).toInt()

        val color = Color.makeRGB(red, green, 0)

        return Paint().apply {
            this.color = color
        }
    }
}

class DrawAtlas(
) {
    val baseColor = Paint().apply {
        color = Color.BLACK
        mode = PaintMode.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    val atlasByState = HashMap<State, StateAtlasEntry>()
//    val transitionByState = HashMap<State, TransitionAtlasEntry>()

    fun add(state: State, entry: StateAtlasEntry) {
        atlasByState[state] = entry
    }

    fun get(state: State): StateAtlasEntry {
        return atlasByState[state] ?: TODO()
    }

    companion object Companion {
        fun <T : Comparable<T>> build(
            extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
            stateAtlasConfig: StateAtlasConfig
        ): DrawAtlas {
            val atlas = DrawAtlas()

            extendedPrefixAutomaton.states.forEachIndexed { index, state ->
                logger.info { "${index / extendedPrefixAutomaton.states.size}%" }
                val paint = stateAtlasConfig.toPaint(state)
                val size = stateAtlasConfig.toSize(state)

                atlas.add(state, StateAtlasEntry(size, paint))
            }

            return atlas
        }
    }
}