package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager
import org.jetbrains.jewel.ui.component.CircularProgressIndicatorBig
import org.jetbrains.jewel.ui.component.GroupHeader
import org.jetbrains.jewel.ui.component.InfoText
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Text

@Composable
fun LayoutUi(
    tabStateManager: TabStateManager,
    modifier: Modifier = Modifier,
) {
    val tabsState by tabStateManager.tabs.collectAsState()
    val activeTabId by tabStateManager.activeTabId.collectAsState()
    val currentTab = remember(tabsState, activeTabId) {
        tabsState.find { it.id == activeTabId }
    }
    val currentLayout by remember(currentTab) {
        mutableStateOf(currentTab?.layoutConfig)
    }

    if (currentLayout == null && activeTabId != null && currentTab != null) {
        CircularProgressIndicatorBig()
    } else {
        val availableLayouts = listOf(
            LayoutConfig.RadialWalker(),
            LayoutConfig.Walker(),
            LayoutConfig.DirectAngular(),
        )
        var layoutSelectionIndex by remember(currentLayout) {
            if (currentLayout != null) {
                mutableIntStateOf(availableLayouts.indexOfFirst { it.name == currentLayout?.name })
            } else {
                mutableIntStateOf(0)
            }
        }

        Column(
//            modifier = modifier.padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GroupHeader("Layout algorithm")
            ListComboBox(
                items = availableLayouts.map { it.name },
                selectedIndex = layoutSelectionIndex,
                onSelectedItemChange = { index ->
                    layoutSelectionIndex = index
                    val newConfig = availableLayouts[index]
                    tabStateManager.updateLayout(
                        activeTabId!!,
                        newConfig
                    )
                },
            )

            GroupHeader("Settings for ${currentLayout?.name}:")
            LayoutConfigUI(currentLayout!!) { newConfig ->
                tabStateManager.updateLayout(
                    activeTabId!!,
                    newConfig
                )
            }
        }
    }
}
