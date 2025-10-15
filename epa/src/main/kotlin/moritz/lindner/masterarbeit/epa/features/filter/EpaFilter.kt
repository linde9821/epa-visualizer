package moritz.lindner.masterarbeit.epa.features.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback

/**
 * Interface for filtering or transforming an [ExtendedPrefixAutomaton].
 *
 * An [EpaFilter] represents a reusable and composable transformation
 * applied to an EPA.
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
    fun apply(
        epa: ExtendedPrefixAutomaton<T>,
        progressCallback: EpaProgressCallback? = null
    ): ExtendedPrefixAutomaton<T>
}
