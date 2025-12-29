package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state.StateInfo
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager
import org.jetbrains.jewel.ui.component.Text

@Composable
fun DetailsUi(
    tabStateManager: TabStateManager,
    epaStateManager: EpaStateManager,
) {
    val tabsState by tabStateManager.tabs.collectAsState()
    val activeTabId by tabStateManager.activeTabId.collectAsState()
    val epaByTabId by epaStateManager.epaByTabId.collectAsState()

    val currentTab = remember(tabsState, activeTabId) {
        tabsState.find { it.id == activeTabId }
    }

    val currentEpa = activeTabId?.let { epaByTabId[it] }

    if (currentTab?.selectedState != null && currentEpa != null && activeTabId != null) {
        StateInfo(
            selectedState = currentTab.selectedState,
            extendedPrefixAutomaton = currentEpa,
            onStateSelected = { state ->
                tabStateManager.setSelectedStateForCurrentTab(state)
                epaStateManager.highlightPathsForState(currentTab.id, state, currentEpa)
            },
            locate = { state ->
                tabStateManager.locateState(state, activeTabId!!)
            }
        )
    } else {
        Text("No state selected.\nTo view details select a state in the visualization by left-clicking it.")
    }
}