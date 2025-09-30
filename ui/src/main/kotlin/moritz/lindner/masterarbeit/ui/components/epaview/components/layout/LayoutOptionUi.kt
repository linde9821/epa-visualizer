package moritz.lindner.masterarbeit.ui.components.epaview.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.ui.components.epaview.components.TabStateManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

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
        CircularProgressIndicator()
    } else {

        val availableLayouts = listOf(
            LayoutConfig.RadialWalker(),
            LayoutConfig.Walker(),
            LayoutConfig.DirectAngular(),
        )
        var layoutSelectionIndex by remember(currentLayout) {
            if (currentLayout != null) {
                mutableStateOf(availableLayouts.indexOfFirst { it.name == currentLayout?.name })
            } else {
                mutableStateOf(0)
            }
        }

        Column(
            modifier = modifier.padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Layout Settings", style = JewelTheme.typography.h1TextStyle)

            Divider(
                orientation = Orientation.Horizontal,
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = JewelTheme.contentColor.copy(alpha = 0.2f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Algorithm:")
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
            }

            LayoutConfigUI(currentLayout!!) { newConfig ->
                tabStateManager.updateLayout(
                    activeTabId!!,
                    newConfig
                )
            }
        }
    }
}
