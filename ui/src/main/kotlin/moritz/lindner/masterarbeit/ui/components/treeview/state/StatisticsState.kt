package moritz.lindner.masterarbeit.ui.components.treeview.state

import moritz.lindner.masterarbeit.epa.features.statistics.Statistics

data class StatisticsState<T : Comparable<T>>(
    val fullEpa: Statistics<T>,
    val filteredEpa: Statistics<T>?,
)
