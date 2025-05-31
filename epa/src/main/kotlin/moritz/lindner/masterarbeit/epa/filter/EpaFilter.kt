package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata

/**
 * Functional interface for filtering or transforming an [ExtendedPrefixAutomata].
 *
 * An [EpaFilter] represents a reusable and composable transformation applied to an EPA.
 * Filters can be chained using [then], or combined via [combine] to build pipelines.
 *
 * @param T The timestamp type used in the automaton's events.
 */
fun interface EpaFilter<T : Comparable<T>> {
    /**
     * Applies this filter to the given [ExtendedPrefixAutomata].
     *
     * @param epa The automaton to transform or filter.
     * @return A new [ExtendedPrefixAutomata] after applying the filter.
     */
    fun apply(epa: ExtendedPrefixAutomata<T>): ExtendedPrefixAutomata<T>

    /**
     * Composes this filter with another, applying `this` first, then [next].
     *
     * @param next The filter to apply after this one.
     * @return A new [EpaFilter] representing the composed transformation.
     */
    fun then(next: EpaFilter<T>): EpaFilter<T> =
        EpaFilter { epa ->
            next.apply(this@EpaFilter.apply(epa))
        }

    companion object {
        /**
         * Combines a list of filters into a single filter that applies them in sequence.
         *
         * @param filters The list of filters to compose.
         * @return A single [EpaFilter] representing the combined application.
         */
        fun <T : Comparable<T>> combine(filters: List<EpaFilter<T>>): EpaFilter<T> =
            filters.reduce { a, b ->
                a.then(b)
            }
    }
}
