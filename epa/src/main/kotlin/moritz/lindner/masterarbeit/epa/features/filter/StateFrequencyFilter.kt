package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaFromComponentsBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequencyVisitor

/**
 * Filters an [ExtendedPrefixAutomaton] by removing all states whose
 * normalized visit frequency is below a given [threshold].
 *
 * The frequency is determined using a [NormalizedStateFrequencyVisitorV2],
 * and only states meeting the threshold (or the [State.Root]) are
 * retained. Orphaned states with missing predecessors are also removed.
 *
 * @param T The timestamp type used in the automaton's events.
 * @property threshold The minimum normalized frequency a state must have
 *    to be retained.
 */
class StateFrequencyFilter<T : Comparable<T>>(
    private val threshold: Float,
) : EpaFilter<T> {

    override val name: String
        get() = "State Frequency Filter"

    /**
     * Applies the state frequency filter to the automaton.
     *
     * @param epa The automaton to filter.
     * @return A new [ExtendedPrefixAutomaton] with only high-frequency states
     *    and valid transitions.
     */
    override fun apply(
        epa: ExtendedPrefixAutomaton<T>,
        progressCallback: EpaProgressCallback?
    ): ExtendedPrefixAutomaton<T> {
        val normalizedStateFrequencyVisitor = NormalizedStateFrequencyVisitor<T>(progressCallback)
        epa.acceptDepthFirst(normalizedStateFrequencyVisitor)
        val normalizedStateFrequency = normalizedStateFrequencyVisitor.build()

        val statesAboveThreshold = epa
            .states
            .filterIndexed { index, state ->
                progressCallback?.onProgress(index, epa.states.size, "$name: filter states")
                when (state) {
                    is State.PrefixState -> normalizedStateFrequency.frequencyByState(state) >= threshold
                    State.Root -> true
                }
            }
            .toSet()

        val epaBuilder = EpaFromComponentsBuilder<T>()
            .fromExisting(epa)
            .setStates(statesAboveThreshold)
            .setProgressCallback(progressCallback)
            .pruneStatesUnreachableByTransitions(true)

        return epaBuilder.build()
    }
}
