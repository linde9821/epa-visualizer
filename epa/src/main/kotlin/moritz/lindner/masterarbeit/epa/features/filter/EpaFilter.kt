package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter.Companion.combine

/**
 * Functional interface for filtering or transforming an [ExtendedPrefixAutomaton].
 *
 * An [EpaFilter] represents a reusable and composable transformation applied to an EPA.
 * Filters can be chained using [then], or combined via [combine] to build pipelines.
 *
 * @param T The timestamp type used in the automaton's events.
 */
interface EpaFilter<T : Comparable<T>> {

    val name: String

    /**
     * Applies this filter to the given [ExtendedPrefixAutomaton].
     *
     * @param epa The automaton to transform or filter.
     * @return A new [ExtendedPrefixAutomaton] after applying the filter.
     */
    fun apply(epa: ExtendedPrefixAutomaton<T>): ExtendedPrefixAutomaton<T>

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
        fun <T : Comparable<T>> combine(filters: List<EpaFilter<T>>): EpaFilter<T> {
            return if (filters.isEmpty()) {
                NoOpFilter()
            } else {
                filters.reduce { a, b ->
                    a.then(b)
                }
            }
        }
    }
}
