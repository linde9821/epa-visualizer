package moritz.lindner.masterarbeit.epa.api

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutFactory
import moritz.lindner.masterarbeit.epa.features.layout.tree.EpaToTree
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
}

class LayoutService<T : Comparable<T>> {

    private val logger = KotlinLogging.logger {}

    // TODO: maybe add caching
    fun buildLayout(
        epa: ExtendedPrefixAutomaton<Long>,
        layoutConfig: LayoutConfig
    ): TreeLayout {
        logger.info { "building tree" }
        val treeVisitor = EpaToTree<Long>()
        epa.copy().acceptDepthFirst(treeVisitor)

        logger.info { "building tree layout" }
        val layout = LayoutFactory.create(layoutConfig)

        layout.build(treeVisitor.root)

        return layout
    }
}