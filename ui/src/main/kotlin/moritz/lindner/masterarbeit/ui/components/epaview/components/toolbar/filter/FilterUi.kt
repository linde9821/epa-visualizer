package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.filter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Text
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
    val tabNames = listOf("Select new filter", "Activity", "State Frequency", "Partition Frequency", "Chain Pruning")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabsState by tabStateManager.tabs.collectAsState()
    val activeTabId by tabStateManager.activeTabId.collectAsState()
    val epaByTabId by epaStateManager.epaByTabId.collectAsState()

    val currentTab = remember(tabsState, activeTabId) {
        tabsState.find { it.id == activeTabId }
    }

    val activeAppliedFilters = remember(tabsState, activeTabId) {
        currentTab?.filters
    }

    val epa = remember(epaByTabId, activeTabId) {
        activeTabId?.let { epaByTabId[it] }
    }

    var currentEditingFilter by remember(currentTab) { mutableStateOf<EpaFilter<Long>?>(null) }
    val newFilters = remember(currentTab) { mutableStateListOf<EpaFilter<Long>>() }

    if (currentTab == null || activeAppliedFilters == null || epa == null) {
        CircularProgressIndicatorBig()
    } else {
        var showActiveFilters by remember { mutableStateOf(true) }
        var showNewFilters by remember { mutableStateOf(true) }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterHeaderSection(
                title = "Active Filters (${currentTab.filters.size})",
                show = showActiveFilters,
                onIconClicked = { showActiveFilters = !showActiveFilters }
            )

            if (showActiveFilters) {
                Filters(activeAppliedFilters)
            }

            Divider(
                orientation = Orientation.Horizontal,
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = JewelTheme.contentColor.copy(alpha = 0.2f)
            )

            FilterHeaderSection(
                title = "New Filters (${newFilters.size})",
                show = showNewFilters,
                onIconClicked = { showNewFilters = !showNewFilters }
            )

            if (showNewFilters) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(.9f)
                ) {
                    ListComboBox(
                        items = tabNames,
                        selectedIndex = selectedTabIndex,
                        onSelectedItemChange = { index ->
                            selectedTabIndex = index
                        },
                        modifier = Modifier.width(200.dp)
                    )

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

                if (selectedTabIndex > 0) {
                    when (selectedTabIndex) {
                        1 -> ActivityFilterTabUi(epa) { currentEditingFilter = it }
                        2 -> StateFrequencyFilterUi(epa, backgroundDispatcher) { currentEditingFilter = it }
                        3 -> PartitionFrequencyFilterUi(epa, backgroundDispatcher) { currentEditingFilter = it }
                        4 -> ChainPruningFilterUi { currentEditingFilter = it }
                        else -> {}
                    }
                }

                Filters(newFilters, allowRemoval = true, onRemove = newFilters::remove)

                DefaultButton(
                    onClick = {
                        val epaService = EpaService<Long>()
                        val filters = currentTab.filters + newFilters
                        val name = epaService.filterNames(filters)
                        // add new tab and so on
                        val id = UUID.randomUUID().toString()
                        tabStateManager.addTab(
                            id = id,
                            title = name,
                            filters = filters,
                            layoutConfig = LayoutConfig.RadialWalker()
                        )
                        tabStateManager.setActiveTab(id)
                    },
                    enabled = newFilters.isNotEmpty()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("New EPA from all filters", style = JewelTheme.typography.regular)
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

@Composable
fun FilterHeaderSection(
    title: String,
    show: Boolean,
    onIconClicked: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            title,
            style = JewelTheme.typography.h2TextStyle
        )

        IconButton(
            onClick = { onIconClicked() },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            if (!show) {
                Icon(AllIconsKeys.General.ChevronDown, "Chevron")
            } else {
                Icon(AllIconsKeys.General.ChevronUp, "Chevron")
            }
        }
    }
}

@Composable
fun Filters(
    filters: List<EpaFilter<Long>>,
    allowRemoval: Boolean = false,
    onRemove: (EpaFilter<Long>) -> Unit = {}
) {
    val size = filters.size
    filters.forEachIndexed { index, filter ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = filter.name,
                    style = JewelTheme.typography.medium
                )
                Text(
                    text = "Filter ${index + 1}",
                    style = JewelTheme.typography.small,
                    color = JewelTheme.contentColor.copy(alpha = 0.7f)
                )
            }

            if (allowRemoval) {
                IconButton(
                    onClick = {
                        onRemove(filter)
                    }
                ) {
                    Icon(
                        key = AllIconsKeys.General.Delete,
                        contentDescription = "Delete filter",
                        tint = JewelTheme.contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Arrow pointing to next filter (except for the last one)
        if (index < size - 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        "THEN",
                        style = JewelTheme.typography.small,
                        color = JewelTheme.contentColor.copy(alpha = 0.6f)
                    )
                    Icon(
                        key = AllIconsKeys.General.ArrowDown,
                        contentDescription = "Next filter",
                        tint = JewelTheme.contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
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
