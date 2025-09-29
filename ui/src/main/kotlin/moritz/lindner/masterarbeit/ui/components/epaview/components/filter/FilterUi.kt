package moritz.lindner.masterarbeit.ui.components.epaview.components.filter

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineDispatcher
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.ui.components.epaview.components.ProjectViewModel
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
import kotlin.math.log10
import kotlin.math.pow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilterUi(
    projectViewModel: ProjectViewModel,
    backgroundDispatcher: CoroutineDispatcher,
    modifier: Modifier = Modifier,
) {
    val tabNames = listOf("Select new filter", "Activity", "State Frequency", "Partition Frequency", "Chain Pruning")
    var selectedTabIndex by remember { mutableStateOf(0) }
    var currentFilter by remember { mutableStateOf<EpaFilter<Long>?>(null) }

    val epa by projectViewModel.activeEpa.collectAsState()
    val currentConfig by projectViewModel.activeConfig.collectAsState()
    val filtersForCurrentEpa by projectViewModel.activeConfig.collectAsState()
    val newFilters = remember { mutableListOf<EpaFilter<Long>>() }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Filters", style = JewelTheme.typography.h1TextStyle)

            DefaultButton(
                onClick = {
                    projectViewModel.applyNewFilters(currentConfig!!, newFilters)
                },
                enabled = true // currentConfig != epaUiState.filters
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Create new EPA with filters", style = JewelTheme.typography.regular)
                    Icon(key = AllIconsKeys.Actions.Rerun, contentDescription = "Apply", tint = JewelTheme.contentColor)
                }
            }
        }

        // Separator after header
        Divider(
            orientation = Orientation.Horizontal,
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = JewelTheme.contentColor.copy(alpha = 0.2f)
        )

        // Filter selection section
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Add New Filter",
                style = JewelTheme.typography.h2TextStyle
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListComboBox(
                    items = tabNames,
                    selectedIndex = selectedTabIndex,
                    onSelectedItemChange = { index ->
                        selectedTabIndex = index
                    },
                    modifier = Modifier.width(140.dp)
                )

                DefaultButton(
                    onClick = {
                        currentFilter?.let { filter ->
                            selectedTabIndex = 0
                            newFilters.add(filter)
                            currentFilter = null
                        }
                    },
                    enabled = currentFilter != null
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

        // Filter configuration section
        if (selectedTabIndex > 0) {
            Column(modifier = Modifier.padding(16.dp)) {
                when (selectedTabIndex) {
                    1 -> ActivityFilterTabUi(epa!!) { currentFilter = it }
                    2 -> StateFrequencyFilterUi(epa!!, backgroundDispatcher) { currentFilter = it }
                    3 -> PartitionFrequencyFilterUi(epa!!, backgroundDispatcher) { currentFilter = it }
                    4 -> ChainPruningFilterUi { currentFilter = it }
                    else -> Text(
                        "${tabNames[selectedTabIndex]} not implemented",
                        style = JewelTheme.typography.regular
                    )
                }
            }
        }

        // Active filters section
        Divider(
            orientation = Orientation.Horizontal,
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = JewelTheme.contentColor.copy(alpha = 0.2f)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Active Filters (${filtersForCurrentEpa?.filters?.size})",
                style = JewelTheme.typography.h2TextStyle
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtersForCurrentEpa
                    ?.filters
                    ?.forEachIndexed { index, filter ->
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
                        }

                        // Arrow pointing to next filter (except for the last one)
                        if (index < (filtersForCurrentEpa?.filters?.size ?: 0) - 1) {
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

                Text(
                    "New Filters (${newFilters.size})",
                    style = JewelTheme.typography.h2TextStyle
                )

                newFilters.forEachIndexed { index, filter ->
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
                                text = "New Filter ${index + 1}",
                                style = JewelTheme.typography.small,
                                color = JewelTheme.contentColor.copy(alpha = 0.7f)
                            )
                            IconButton(
                                onClick = {
                                    newFilters.removeAt(index)
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
                    if (index < newFilters.size - 1) {
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
