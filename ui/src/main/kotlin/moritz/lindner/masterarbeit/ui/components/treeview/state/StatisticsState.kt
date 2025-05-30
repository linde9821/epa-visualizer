package moritz.lindner.masterarbeit.ui.components.treeview.state

import moritz.lindner.masterarbeit.epa.visitor.statistics.Statistics

data class StatisticsState(
    val fullEpa: Statistics,
    val filteredEpa: Statistics?,
)
