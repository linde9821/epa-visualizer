package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.layout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.CircularProgressIndicatorBig
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.GroupHeader
import org.jetbrains.jewel.ui.component.ListComboBox

@Composable
fun LayoutUi(
    tabStateManager: TabStateManager,
    epaStateManager: EpaStateManager,
) {
    val tabsState by tabStateManager.tabs.collectAsState()
    val activeTabId by tabStateManager.activeTabId.collectAsState()
    val epaByTabId by epaStateManager.epaByTabId.collectAsState()
    val stateLabelsByTabId by epaStateManager.stateLabelsByTabId.collectAsState()
    val currentTab = remember(tabsState, activeTabId) {
        tabsState.find { it.id == activeTabId }
    }
    val currentLabels by remember(currentTab) {
        mutableStateOf(stateLabelsByTabId[currentTab?.id])
    }
    var currentLayoutConfig by remember(currentTab) {
        mutableStateOf(currentTab?.layoutConfig)
    }

    val currentEpa = activeTabId?.let { epaByTabId[it] }

    if (currentLayoutConfig == null && activeTabId != null && currentTab != null) {
        CircularProgressIndicatorBig()
    } else {
        val availableLayouts = listOfNotNull(
            LayoutConfig.RadialWalkerConfig(),
            LayoutConfig.WalkerConfig(),
            LayoutConfig.DirectAngularConfig(),
            LayoutConfig.PartitionSimilarityRadialLayoutConfig(),
            LayoutConfig.StateClusteringLayoutConfig(),
            LayoutConfig.PartitionClusteringLayoutConfig(),
            currentEpa?.let {
                LayoutConfig.RadialWalkerTimeConfig(
                    extendedPrefixAutomaton = currentEpa
                )
            },
            currentLabels?.let {
                LayoutConfig.PRTLayoutConfig(labelSizeByState = currentLabels!!.getLabelSizeMap())
            },
        )

        var layoutSelectionIndex by remember(currentLayoutConfig) {
            if (currentLayoutConfig != null) {
                mutableIntStateOf(availableLayouts.indexOfFirst { it.name == currentLayoutConfig?.name })
            } else {
                mutableIntStateOf(0)
            }
        }

        GroupHeader("Layout algorithm:")
        ListComboBox(
            items = availableLayouts.map(LayoutConfig::name),
            selectedIndex = layoutSelectionIndex,
            onSelectedItemChange = { index ->
                currentLayoutConfig = availableLayouts[index]
            },
        )

        Divider(
            orientation = Orientation.Horizontal,
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = JewelTheme.contentColor.copy(alpha = 0.2f)
        )

        GroupHeader("Settings for ${currentLayoutConfig?.name}:")
        LayoutConfigUI(currentLayoutConfig!!) { configUpdate ->
            if (configUpdate != currentTab?.layoutConfig) {
                tabStateManager.updateLayout(
                    activeTabId!!,
                    configUpdate
                )
            }
        }
    }
}
