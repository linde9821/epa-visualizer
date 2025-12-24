package moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.semantic

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import kotlin.math.log10

object LogarithmicCycleTimeCalculator {

    private val epaService = EpaService<Long>()

    fun logarithmicMinMaxNormalizedTransitionCycleTimeByTransition(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        configMin: Float,
        configMax: Float,
        progressCallback: EpaProgressCallback? = null
    ): Map<Transition, Float> {
        val cycleTimes = epaService.computeStateTransitionCycleTimesOfAllStates(
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
        // Add offset to handle zero values with logarithmic scaling
        val valuesWithoutRootAndTerminating = cycleTimes
            .filterKeys {
                it.start != State.Root &&
                        epaService.isFinalState(extendedPrefixAutomaton, it.start).not()
            }
            .values
            .map { it + offset }

        val min = valuesWithoutRootAndTerminating.minOrNull() ?: offset
        val max = valuesWithoutRootAndTerminating.maxOrNull() ?: offset

        val transitionCycleTimeByTransition: Map<Transition, Float> = if ((max - min) < 0.0001f) {
            // All values are essentially the same - use middle of range
            extendedPrefixAutomaton.transitions.associateWith { (configMin + configMax) / 2 }
        } else {
            val logMin = log10(min)
            val logMax = log10(max)

            extendedPrefixAutomaton.transitions.associateWith { transition ->
                val rawValue = cycleTimes[transition]!!
                val value = rawValue + offset
                val logValue = log10(value)

                val normalized = normalized(logValue, logMin, logMax, min, max)
                configMin + normalized * (configMax - configMin)
            }
        }

        return transitionCycleTimeByTransition
    }

    fun logarithmicMinMaxNormalizedAverageCycleTimeByState(
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

    fun combinedLogarithmicMinMaxNormalizedTransitionCycleTimeByState(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        configMin: Float,
        configMax: Float,
        progressCallback: EpaProgressCallback? = null
    ): Map<State, Float> {
        val transitionCycleTimeByTransition = logarithmicMinMaxNormalizedTransitionCycleTimeByTransition(
            extendedPrefixAutomaton = extendedPrefixAutomaton,
            configMin = configMin,
            configMax = configMax,
            progressCallback = progressCallback
        )

        return extendedPrefixAutomaton.states.associateWith { state ->
            when (state) {
                is State.PrefixState -> transitionCycleTimeSumUpOfPathFrom(
                    extendedPrefixAutomaton,
                    state,
                    transitionCycleTimeByTransition
                )

                State.Root -> 0f
            }
        }
    }

    private fun transitionCycleTimeSumUpOfPathFrom(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        state: State.PrefixState,
        transitionCycleTimeByTransition: Map<Transition, Float>
    ): Float {
        return epaService
            .getPathFromRoot(state)
            .map { state ->
                when (state) {
                    is State.PrefixState -> {
                        val incoming = extendedPrefixAutomaton.incomingTransitionsByState[state]!!.first()
                        transitionCycleTimeByTransition[incoming]!!
                    }

                    State.Root -> 0.0f
                }
            }.sum()
    }
}