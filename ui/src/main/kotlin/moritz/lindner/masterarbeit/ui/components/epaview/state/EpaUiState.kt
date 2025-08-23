package moritz.lindner.masterarbeit.ui.components.epaview.state

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout

data class EpaUiState(
    val layout: TreeLayout?,
    val isLoading: Boolean,
    val filteredEpa: ExtendedPrefixAutomaton<Long>?,
    val filters: List<EpaFilter<Long>>
)
