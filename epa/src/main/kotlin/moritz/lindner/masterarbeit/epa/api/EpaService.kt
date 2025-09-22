package moritz.lindner.masterarbeit.epa.api

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.dot.DotExport
import moritz.lindner.masterarbeit.epa.features.statistics.Statistics
import moritz.lindner.masterarbeit.epa.features.statistics.StatisticsVisitor
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter

class EpaService<T : Comparable<T>> {

    fun getStatistics(epa: ExtendedPrefixAutomaton<T>): Statistics<T> {
        val visitor = StatisticsVisitor<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    fun applyFilters(epa: ExtendedPrefixAutomaton<T>, filters: List<EpaFilter<T>>): ExtendedPrefixAutomaton<T> {
        return filters.fold(epa) { acc, filter -> filter.apply(acc) }
    }

    fun getPartitionInfo(epa: ExtendedPrefixAutomaton<T>): PartitionInfo {
        return PartitionInfo(
            partitions = epa.getAllPartitions(),
            totalStates = epa.states.size,
            totalActivities = epa.activities.size
        )
    }

    fun exportToDot(epa: ExtendedPrefixAutomaton<T>): String {
        val visitor = DotExport<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.dot
    }
}

data class PartitionInfo(
    val partitions: List<Int>,
    val totalStates: Int,
    val totalActivities: Int
)
