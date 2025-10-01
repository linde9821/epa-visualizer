package moritz.lindner.masterarbeit.ui.components.epaview.components.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TidyTreeUi
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CircularProgressIndicatorBig
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
    backgroundDispatcher: ExecutorCoroutineDispatcher
) {
    val tabsState by tabStateManager.tabs.collectAsState()
    val activeTabId by tabStateManager.activeTabId.collectAsState()
    val epaByTabId by epaStateManager.epaByTabId.collectAsState()
    val layoutByTabId by epaStateManager.layoutAndConfigByTabId.collectAsState()
    val stateLabelsByTabId by epaStateManager.stateLabelsByTabId.collectAsState()
    val animationState by epaStateManager.animationState.collectAsState()

    val currentTab = remember(tabsState, activeTabId) {
        tabsState.find { it.id == activeTabId }
    }

    val currentProgress = currentTab?.progress
    val currentEpa = activeTabId?.let { epaByTabId[it] }
    val currentLayoutAndConfig = activeTabId?.let { layoutByTabId[it] }
    val currentStateLabels = activeTabId?.let { stateLabelsByTabId[it] }

    val interactionSource = remember { MutableInteractionSource() }

    val tabs =
        remember(tabsState, activeTabId) {
            tabsState.map { epaTab ->
                TabData.Editor(
                    selected = epaTab.id == activeTabId,
                    content = { tabState ->
                        SimpleTabContent(
                            state = tabState,
                            modifier = Modifier.Companion,
                            icon = {
                                Icon(
                                    key = AllIconsKeys.Graph.Layout,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp).tabContentAlpha(state = tabState),
                                )
                            },
                            label = { Text(epaTab.title) },
                        )
                    },
                    onClose = {
                        if (tabsState.size > 1) {
                            tabStateManager.removeTab(epaTab.id)
                            epaStateManager.removeAllForTab(epaTab.id)
                        }
                    },
                    onClick = {
                        tabStateManager.setActiveTab(epaTab.id)
                    },
                )
            }
        }

    Column {
        TabStrip(
            tabs = tabs,
            style = JewelTheme.defaultTabStyle,
            interactionSource = interactionSource
        )
        if (currentTab != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                if ((currentProgress != null && !currentProgress.isComplete)) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalProgressBar(
                            progress = currentProgress.percentage,
                            modifier = Modifier.width(200.dp)
                        )
                        Text("${currentProgress.taskName}: ${currentProgress.current} / ${currentProgress.total}")
                    }
                } else if (currentEpa != null && currentLayoutAndConfig != null && currentStateLabels != null) {
                    // TODO: does this need to be a column
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (currentLayoutAndConfig.first.isBuilt()) {
                            TidyTreeUi(
                                treeLayout = currentLayoutAndConfig.first,
                                stateLabels = currentStateLabels,
                                animationState = animationState
                            )
                        } else {
                            Text("Rendering is disabled")
                        }
                    }
                } else {
                    Box(
                        modifier = modifier.fillMaxSize().background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicatorBig(Modifier.align(Alignment.Center).size(50.dp))
                    }
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