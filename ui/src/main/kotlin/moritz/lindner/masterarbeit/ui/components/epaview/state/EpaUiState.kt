package moritz.lindner.masterarbeit.ui.components.epaview.state

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout

data class EpaUiState(
    val layout: TreeLayout?,
    val isLoading: Boolean,
    val filteredEpa: ExtendedPrefixAutomata<Long>?,
)
