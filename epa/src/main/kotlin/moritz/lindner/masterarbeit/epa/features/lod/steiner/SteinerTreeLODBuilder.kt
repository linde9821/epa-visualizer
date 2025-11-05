package moritz.lindner.masterarbeit.epa.features.lod.steiner

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequency

class SteinerTreeLODBuilder<T : Comparable<T>>(private val epa: ExtendedPrefixAutomaton<T>) {

    private val solver = SteinerTreeSolver(epa)
    private val epaService = EpaService<T>()

    /** Build LOD levels using Steiner tree approximation */
    suspend fun buildLODLevels(): List<SteinerLODLevel> {
        val normalizedFrequency = epaService.getNormalizedStateFrequency(epa)

        val thresholds = listOf(0.0f, 0.001f)

        return coroutineScope {
            thresholds.map { threshold ->
                async {
                    val terminals = selectTerminals(threshold, normalizedFrequency)
                    solver.computeSteinerTree(terminals)
                }
            }.awaitAll()
        }
    }

    private fun selectTerminals(threshold: Float, normalizedFrequency: NormalizedStateFrequency): Set<State> {
        val terminals = mutableSetOf<State>()

        // Always include root
        terminals.add(State.Root)

        // Include high-frequency states
        for (state in epa.states) {
            val freq = normalizedFrequency.frequencyByState(state)
            if (freq >= threshold) {
                terminals.add(state)
            }
        }

        return terminals
    }
}