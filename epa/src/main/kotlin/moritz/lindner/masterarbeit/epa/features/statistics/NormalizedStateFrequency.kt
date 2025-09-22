package moritz.lindner.masterarbeit.epa.features.statistics

import moritz.lindner.masterarbeit.epa.domain.State

class NormalizedStateFrequency(
    private val relativeFrequencyByState: HashMap<State, Float>
) {
    /**
     * Returns the normalized frequency for the given [state].
     *
     * @param state The state whose frequency to retrieve.
     * @return The normalized frequency as a float between 0.0 and 1.0.
     * @throws NullPointerException if the state was not visited or processed.
     */
    fun frequencyByState(state: State): Float = relativeFrequencyByState[state]!!


    /**
     * Returns the minimum normalized frequency across all states.
     */
    fun min(): Float = relativeFrequencyByState.values.min()

    /**
     * Returns the maximum normalized frequency across all states.
     */
    fun max(): Float = relativeFrequencyByState.values.max()
}