package moritz.lindner.masterarbeit.ui.components.epaview.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.HorizontalProgressBar
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.SimpleTabContent
import org.jetbrains.jewel.ui.component.TabData
import org.jetbrains.jewel.ui.component.TabStrip
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.defaultTabStyle

@Composable
fun TabsComponent(
    tabStateManager: TabStateManager,
    epaStateManager: EpaStateManager,
    modifier: Modifier = Modifier.Companion,
) {
    val tabsState by tabStateManager.tabs.collectAsState()
    val activeTabId by tabStateManager.activeTabId.collectAsState()
    val epaByTabId by epaStateManager.epaByTabId.collectAsState()

    val currentTab = remember(tabsState, activeTabId) {
        tabsState.find { it.id == activeTabId }
    }

    val currentProgress = remember(currentTab) {
        currentTab?.progress
    }

    val currentEpa = remember(epaByTabId, activeTabId) {
        activeTabId?.let { epaByTabId[it] }
    }

    LaunchedEffect(activeTabId) {
        activeTabId?.let { tabId ->
            if (epaByTabId[tabId] == null) {
                epaStateManager.buildEpaForTab(tabId)
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    val tabs =
        remember(tabsState, activeTabId) {
            tabsState.mapIndexed { index, epaTab ->
                TabData.Editor(
                    selected = epaTab.id == activeTabId,
                    content = { tabState ->
                        SimpleTabContent(
                            state = tabState,
                            modifier = Modifier.Companion,
                            icon = {
                                Icon(
                                    key = AllIconsKeys.Actions.Find,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp).tabContentAlpha(state = tabState),
                                    tint = Color.Magenta,
                                )
                            },
                            label = { Text(epaTab.title) },
                        )
                    },
                    onClose = {
                        if (tabsState.size > 1) {
                            tabStateManager.removeTab(epaTab.id)
                            epaStateManager.removeEpaForTab(epaTab.id)
                        }
                    },
                    onClick = {
                        tabStateManager.setActiveTab(epaTab.id)
                    },
                )
            }
        }

    TabStrip(
        tabs = tabs,
        style = JewelTheme.defaultTabStyle,
        interactionSource = interactionSource
    )

    Column(modifier = modifier) {
        // Tab content
        val activeTab = tabsState.find { it.id == activeTabId }
        if (activeTab != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                if ((currentProgress != null && !currentProgress.isComplete) || currentEpa == null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalProgressBar(
                            progress = currentProgress?.percentage ?: 0f,
                            modifier = Modifier.width(200.dp)
                        )
                        Text("${currentProgress?.taskName ?: "loading"}: ${currentProgress?.current ?: 0f} / ${currentProgress?.total ?: 1f}")
                    }
                } else {
                    // Show EPA content (placeholder for now)
                    Text(
                        "EPA PLACEHOLDER: Tab: ${activeTab.title} - Config: ${activeTab.id}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nothing to see because no tabs available")
            }
        }
    }
}