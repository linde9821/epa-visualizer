package moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.semantic

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import kotlin.math.log10

object LogarithmicCycleTimeCalculator {

    private val epaService = EpaService<Long>()

    fun logarithmicMinMaxNormalizedCycleTimeByState(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        min: Float,
        max: Float,
        progressCallback: EpaProgressCallback? = null
    ): Map<State, Float> {
        return buildMap {
            val cycleTimes = epaService.computeAverageStateCycleTimesOfAllStates(
                extendedPrefixAutomaton = extendedPrefixAutomaton,
                minus = Long::minus,
                average = { cycleTimes ->
                    if (cycleTimes.isEmpty()) {
                        0f
                    } else cycleTimes.average().toFloat()
                },
                progressCallback = progressCallback
            )

            val offset = 1.0f
            val valuesWithoutRootAndTerminating = cycleTimes
                .filterKeys { state ->
                    state != State.Root && epaService.isFinalState(extendedPrefixAutomaton, state).not()
                }
                .values
                .map { cycleTime -> cycleTime + offset }

            val rawMin = valuesWithoutRootAndTerminating.minOrNull() ?: offset
            val rawMax = valuesWithoutRootAndTerminating.maxOrNull() ?: offset

            val logMin = log10(rawMin)
            val logMax = log10(rawMax)

            extendedPrefixAutomaton.states.forEach { state ->
                when (state) {
                    is State.PrefixState -> {
                        // 1. log scaling
                        val rawValue = cycleTimes[state]!!
                        val value = rawValue + offset
                        val logValue = log10(value)

                        // 2. normalization
                        val normalized = normalized(logValue, logMin, logMax, min, max)

                        // 3. min-max
                        val minMaxNormalized = min + normalized * (max - min)

                        put(state, minMaxNormalized)
                    }

                    State.Root -> {
                        put(state, min + 0f * (max - min))
                    }
                }
            }
        }
    }

    private fun normalized(logValue: Float, logMin: Float, logMax: Float, min: Float, max: Float): Float {
        return when {
            logMax == logMin -> (min + max) / 2
            else -> ((logValue - logMin) / (logMax - logMin)).coerceIn(0.0f, 1.0f)
        }
    }

    fun combinedLogarithmicMinMaxNormalizedCycleTimeByState(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        min: Float,
        max: Float,
        progressCallback: EpaProgressCallback? = null
    ): Map<State, Float> {
        val logarithmicNormalizedCycleTimeByState = logarithmicMinMaxNormalizedCycleTimeByState(
            extendedPrefixAutomaton,
            min,
            max,
            progressCallback,
        )

        return extendedPrefixAutomaton.states.associateWith { state ->
            when (state) {
                is State.PrefixState -> cycleTimeSum(state, logarithmicNormalizedCycleTimeByState)
                State.Root -> 0f
            }
        }
    }


    private fun cycleTimeSum(state: State.PrefixState, cycleTimes: Map<State, Float>): Float {
        return epaService
            .getPathFromRoot(state)
            .map { stateOnPath -> cycleTimes[stateOnPath]!! }
            .sum()
    }
}