package moritz.lindner.masterarbeit.ui.components.treeview.state

import moritz.lindner.masterarbeit.epa.drawing.layout.TreeLayout

data class UiState(
    val layout: TreeLayout?,
    val isLoading: Boolean,
    val statistics: Float?,
)
