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

class EpaService<T : Comparable<T>> {

    fun getStatistics(epa: ExtendedPrefixAutomaton<T>): Statistics<T> {
        val visitor = StatisticsVisitor<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    fun applyFilters(epa: ExtendedPrefixAutomaton<T>, filters: List<EpaFilter<T>>): ExtendedPrefixAutomaton<T> {
        return filters.fold(epa) { acc, filter -> filter.apply(acc) }
    }

    fun getEventsByCase(epa: ExtendedPrefixAutomaton<T>): Map<String, List<Event<T>>> {
        val visitor = EventsByCasesCollector<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    fun getNormalizedStateFrequency(epa: ExtendedPrefixAutomaton<T>): NormalizedStateFrequency {
        val visitor = NormalizedStateFrequencyVisitor<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    fun getNormalizedPartitionFrequency(epa: ExtendedPrefixAutomaton<T>): NormalizedPartitionFrequency {
        val visitor = NormalizedPartitionFrequencyVisitor<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    fun getParitionsSorted()
}
