package moritz.lindner.masterarbeit.epa.features.statistics

import moritz.lindner.masterarbeit.epa.domain.State

/**
 * Contains normalized frequency statistics for states in an Extended Prefix Automaton.
 *
 * @param relativeFrequencyByState Map of states to their normalized frequencies.
 */
class NormalizedStateFrequency(
    private val relativeFrequencyByState: HashMap<State, Float>
) {
    /**
     * Returns the normalized frequency for the given [state].
     *
     * @param state The state whose frequency to retrieve.
     * @return The normalized frequency as a float between 0.0 and 1.0.
     * @throws IllegalStateException if the state was not visited or processed.
     */
    fun frequencyByState(state: State): Float {
        return relativeFrequencyByState[state]
            ?: throw IllegalStateException("State $state was not visited or processed.")
    }

    /** Returns the minimum normalized frequency across all states. */
    fun min(): Float = relativeFrequencyByState.values.min()

    /**
     * Returns the maximum normalized frequency across all states.
     */
    fun max(): Float = relativeFrequencyByState.values.max()
}