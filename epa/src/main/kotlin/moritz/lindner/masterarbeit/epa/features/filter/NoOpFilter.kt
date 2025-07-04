package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton

/**
 * A no-op implementation of [EpaFilter] that returns the input [ExtendedPrefixAutomaton] unchanged.
 *
 * This can be useful as a default filter, placeholder, or base case in filter compositions.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class NoOpFilter<T : Comparable<T>> : EpaFilter<T> {
    /**
     * Returns the original [ExtendedPrefixAutomaton] without any modifications.
     */
    override fun apply(epa: ExtendedPrefixAutomaton<T>): ExtendedPrefixAutomaton<T> = epa
}
