package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback

/**
 * A no-op implementation of [EpaFilter] that returns the input [ExtendedPrefixAutomaton] unchanged.
 *
 * This can be useful as a default filter, placeholder, or base case in filter compositions.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class NoOpFilter<T : Comparable<T>> : EpaFilter<T> {
    override val name: String = "NoOp Filter"

    /**
     * Returns the original [ExtendedPrefixAutomaton] without any modifications.
     */
    override fun apply(
        epa: ExtendedPrefixAutomaton<T>,
        progressCallback: EpaProgressCallback?
    ): ExtendedPrefixAutomaton<T> = epa
}
