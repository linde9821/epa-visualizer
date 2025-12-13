package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.filter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineDispatcher
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.CircularProgressIndicatorBig
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
import java.util.UUID
import kotlin.math.log10
import kotlin.math.pow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilterUi(
    tabStateManager: TabStateManager,
    epaStateManager: EpaStateManager,
    backgroundDispatcher: CoroutineDispatcher,
) {
    val tabNames =
        listOf("Select new filter", "Activity", "State Frequency", "Partition Frequency", "Chain Compression")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabsState by tabStateManager.tabs.collectAsState()
    val activeTabId by tabStateManager.activeTabId.collectAsState()
    val epaByTabId by epaStateManager.epaByTabId.collectAsState()

    val currentTab = remember(tabsState, activeTabId) {
        tabsState.find { it.id == activeTabId }
    }

    val currentlyAppliedFilters = remember(tabsState, activeTabId) {
        currentTab?.filters
    }

    val epa = remember(epaByTabId, activeTabId) {
        activeTabId?.let { epaByTabId[it] }
    }

    var currentEditingFilter by remember(currentTab) { mutableStateOf<EpaFilter<Long>?>(null) }
    val newFilters = remember(currentTab) { mutableStateListOf<EpaFilter<Long>>() }

    if (currentTab == null || currentlyAppliedFilters == null || epa == null) {
        CircularProgressIndicatorBig()
    } else {
        var showActiveFilters by remember { mutableStateOf(true) }
        var showNewFilters by remember { mutableStateOf(true) }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterHeaderSection(
                title = "Active filters (${currentTab.filters.size})",
                show = showActiveFilters,
                onIconClicked = { showActiveFilters = !showActiveFilters }
            )

            if (showActiveFilters) {
                FilterOverview(currentlyAppliedFilters)
            }

            Divider(
                orientation = Orientation.Horizontal,
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = JewelTheme.contentColor.copy(alpha = 0.2f)
            )

            FilterHeaderSection(
                title = "New filters (${newFilters.size})",
                show = showNewFilters,
                onIconClicked = { showNewFilters = !showNewFilters }
            )

            if (showNewFilters) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListComboBox(
                        items = tabNames,
                        selectedIndex = selectedTabIndex,
                        onSelectedItemChange = { index ->
                            selectedTabIndex = index
                        },
                        modifier = Modifier.width(200.dp)
                    )

                    Tooltip(
                        tooltip = {
                            Text(
                                "Add the current filter to the list of new filters. " +
                                        "The filter will not be applied directly."
                            )
                        }
                    ) {
                        DefaultButton(
                            onClick = {
                                currentEditingFilter?.let { filter ->
                                    selectedTabIndex = 0
                                    newFilters.add(filter)
                                    currentEditingFilter = null
                                }
                            },
                            enabled = currentEditingFilter != null
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Add", style = JewelTheme.typography.regular)
                                Icon(
                                    key = AllIconsKeys.Actions.AddList,
                                    contentDescription = "Add filter",
                                    tint = JewelTheme.contentColor
                                )
                            }
                        }
                    }
                }

                if (selectedTabIndex > 0) {
                    when (selectedTabIndex) {
                        1 -> ActivityFilterTabUi(epa) { currentEditingFilter = it }
                        2 -> StateFrequencyFilterUi(epa, backgroundDispatcher) { currentEditingFilter = it }
                        3 -> PartitionFrequencyFilterUi(epa, backgroundDispatcher) { currentEditingFilter = it }
                        4 -> ChainCompressionFilterUi { currentEditingFilter = it }
                        else -> {}
                    }
                }

                if (newFilters.isNotEmpty()) {
                    Divider(
                        orientation = Orientation.Horizontal,
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = JewelTheme.contentColor.copy(alpha = 0.2f)
                    )
                }

                FilterOverview(newFilters, allowRemoval = true, onRemove = newFilters::remove)

                Divider(
                    orientation = Orientation.Horizontal,
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = JewelTheme.contentColor.copy(alpha = 0.2f)
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DefaultButton(
                        onClick = {
                            val epaService = EpaService<Long>()
                            val filters = currentTab.filters + newFilters
                            val name = epaService.filterAsNames(filters)
                            val id = UUID.randomUUID().toString()
                            tabStateManager.addTab(
                                id = id,
                                title = name,
                                filters = filters,
                                layoutConfig = LayoutConfig.RadialWalkerConfig()
                            )
                            tabStateManager.setActiveTab(id)
                        },
                        enabled = newFilters.isNotEmpty()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("Create filtered EPA in new tab", style = JewelTheme.typography.regular)
                            Icon(
                                key = AllIconsKeys.Actions.Rerun,
                                contentDescription = "Apply",
                                tint = JewelTheme.contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

fun sliderToThreshold(
    sliderValue: Float,
    minThreshold: Float,
    maxThreshold: Float,
): Float {
    if (minThreshold <= 0f || maxThreshold <= 0f || minThreshold >= maxThreshold) {
        return minThreshold
    }

    val rangeLog = log10(maxThreshold / minThreshold)
    return 10f.pow(sliderValue * rangeLog) * minThreshold
}
