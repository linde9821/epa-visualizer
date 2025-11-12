package moritz.lindner.masterarbeit.epa.features.lod.steiner

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequency

class SteinerTreeLODBuilder<T : Comparable<T>>(private val epa: ExtendedPrefixAutomaton<T>) {

    private val logger = KotlinLogging.logger { }
    private val solver = SteinerTreeSolver(epa)
    private val epaService = EpaService<T>()

    /** Build LOD levels using Steiner tree approximation */
    fun buildLODLevels(): List<SteinerLODLevel> {
        logger.debug { "building steiner lod levels" }
        val normalizedFrequency = epaService.getNormalizedStateFrequency(epa)

        val thresholds = listOf(0.0f, 0.001f)

        return thresholds.map { threshold ->
            val terminals = selectTerminals(threshold, normalizedFrequency)
            logger.debug { "solving steiner tree for threshold $threshold" }
            solver.computeSteinerTree(terminals)
        }
    }

    private fun selectTerminals(threshold: Float, normalizedFrequency: NormalizedStateFrequency): Set<State> {
        val terminals = mutableSetOf<State>()

        // Always include root
        terminals.add(State.Root)

        for (state in epa.states) {
            val freq = normalizedFrequency.frequencyByState(state)
            if (freq >= threshold) {
                terminals.add(state)
            }
        }

        return terminals
    }
}