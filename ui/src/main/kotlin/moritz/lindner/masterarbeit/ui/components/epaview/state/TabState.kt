package moritz.lindner.masterarbeit.ui.components.epaview.state

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig

/** Represents a single tab with metadata and progress */
data class TabState(
    val id: String,
    val title: String,
    val filters: List<EpaFilter<Long>> = emptyList(),
    val layoutConfig: LayoutConfig,
    val selectedState: State? = null,
    val locateState: State? = null,
    val exportImage: Boolean = false
)