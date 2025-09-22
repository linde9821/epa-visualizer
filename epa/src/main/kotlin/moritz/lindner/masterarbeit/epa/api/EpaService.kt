package moritz.lindner.masterarbeit.epa.api

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.features.animation.EventsByCasesCollector
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequency
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequencyVisitor
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequency
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequencyVisitor
import moritz.lindner.masterarbeit.epa.features.statistics.Statistics
import moritz.lindner.masterarbeit.epa.features.statistics.StatisticsVisitor

/**
 * Service for analyzing and manipulating Extended Prefix Automatons.
 *
 * @param T The type of timestamps used in the event log.
 */
class EpaService<T : Comparable<T>> {

    /**
     * Computes general statistics for the EPA.
     *
     * @param epa The Extended Prefix Automaton to analyze.
     * @return Statistics containing event counts, case counts, and activity frequencies.
     */
    fun getStatistics(epa: ExtendedPrefixAutomaton<T>): Statistics<T> {
        val visitor = StatisticsVisitor<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    /**
     * Applies a list of filters to the EPA sequentially.
     *
     * @param epa The Extended Prefix Automaton to filter.
     * @param filters The list of filters to apply in order.
     * @return A new filtered Extended Prefix Automaton.
     */
    fun applyFilters(epa: ExtendedPrefixAutomaton<T>, filters: List<EpaFilter<T>>): ExtendedPrefixAutomaton<T> {
        return filters.fold(epa) { acc, filter -> filter.apply(acc) }
    }

    /**
     * Groups all events by their case identifiers.
     *
     * @param epa The Extended Prefix Automaton to analyze.
     * @return A map from case ID to list of events for that case.
     */
    fun getEventsByCase(epa: ExtendedPrefixAutomaton<T>): Map<String, List<Event<T>>> {
        val visitor = EventsByCasesCollector<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    /**
     * Computes normalized frequency statistics for each state.
     *
     * @param epa The Extended Prefix Automaton to analyze.
     * @return Normalized state frequency data.
     */
    fun getNormalizedStateFrequency(epa: ExtendedPrefixAutomaton<T>): NormalizedStateFrequency {
        val visitor = NormalizedStateFrequencyVisitor<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    /**
     * Computes normalized frequency statistics for each partition.
     *
     * @param epa The Extended Prefix Automaton to analyze.
     * @return Normalized partition frequency data.
     */
    fun getNormalizedPartitionFrequency(epa: ExtendedPrefixAutomaton<T>): NormalizedPartitionFrequency {
        val visitor = NormalizedPartitionFrequencyVisitor<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }
}
