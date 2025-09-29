package moritz.lindner.masterarbeit.ui.components.epaview.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import kotlin.collections.get

@Composable
fun TabsComponent(
    viewModel: ProjectViewModel,
    modifier: Modifier = Modifier.Companion
) {
    val tabsState by viewModel.tabsByConfigId.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val tabProgress by viewModel.tabProgressByConfigId.collectAsState()
    val currentProgress by remember(tabProgress) { mutableStateOf(tabProgress[activeTabId]) }
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
                                    modifier = Modifier.Companion.size(16.dp).tabContentAlpha(state = tabState),
                                    tint = Color.Companion.Magenta,
                                )
                            },
                            label = { Text(epaTab.title) },
                        )
                    },
                    onClose = {

//                        tabIds = tabIds.toMutableList().apply { removeAt(index) }
//                        if (selectedTabIndex >= index) {
//                            val maxPossibleIndex = max(0, tabIds.lastIndex)
//                            selectedTabIndex = (selectedTabIndex - 1).coerceIn(0..maxPossibleIndex)
//                        }
                    },
                    onClick = { TODO() },
                )
            }
        }

    TabStrip(
        tabs = tabs,
        style = JewelTheme.Companion.defaultTabStyle,
        interactionSource = interactionSource
    )

    Column(modifier = modifier) {
        // Tab content
        val activeTab = tabsState.find { it.id == activeTabId }
        if (activeTab != null) {
            Box(modifier = Modifier.Companion.fillMaxSize()) {
                if (currentProgress?.isActive == true && currentProgress != null) {
                    Column(
                        modifier = Modifier.Companion.align(Alignment.Companion.Center),
                        horizontalAlignment = Alignment.Companion.CenterHorizontally
                    ) {
                        HorizontalProgressBar(
                            progress = currentProgress!!.progress(),
                            modifier = Modifier.Companion.width(200.dp)
                        )
                    }
                } else {
                    // Show EPA content (placeholder for now)
                    Text(
                        "Tab: ${activeTab.title} - Config: ${activeTab.configId}",
                        modifier = Modifier.Companion.align(Alignment.Companion.Center)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.Companion.fillMaxSize(),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text("Nothing to see")
            }
        }
    }
}