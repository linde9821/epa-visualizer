package moritz.lindner.masterarbeit.ui.components.treeview.state

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.drawing.layout.TreeLayout

data class EpaUiState(
    val layout: TreeLayout?,
    val isLoading: Boolean,
    val filteredEpa: ExtendedPrefixAutomata<Long>?,
)
