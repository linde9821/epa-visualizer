package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata

/**
 * A no-op implementation of [EpaFilter] that returns the input [ExtendedPrefixAutomata] unchanged.
 *
 * This can be useful as a default filter, placeholder, or base case in filter compositions.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class DoNothingFilter<T : Comparable<T>> : EpaFilter<T> {
    /**
     * Returns the original [ExtendedPrefixAutomata] without any modifications.
     */
    override fun apply(epa: ExtendedPrefixAutomata<T>): ExtendedPrefixAutomata<T> = epa
}
