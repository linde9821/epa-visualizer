package moritz.lindner.masterarbeit.ui.components.epaview.components.project

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.analysis.state.StateInfo
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.filter.FilterUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.layout.LayoutUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.project.ProjectOverviewUi
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.ProjectStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager
import org.jetbrains.jewel.ui.component.CircularProgressIndicatorBig
import org.jetbrains.jewel.ui.component.InfoText

@Composable
fun SidePanelContent(
    upperState: EpaViewUpperState,
    projectState: ProjectStateManager,
    tabStateManager: TabStateManager,
    epaStateManager: EpaStateManager,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit
) {
    when (upperState) {
        EpaViewUpperState.Project -> PanelMenu(
            "Project Settings",
            modifier = Modifier.padding(8.dp),
            onClose = { onClose() }
        ) {
            ProjectOverviewUi(projectState)
        }

        EpaViewUpperState.Filter -> PanelMenu(
            "Filter Settings",
            modifier = Modifier.padding(4.dp),
            onClose = { onClose() }
        ) {
            FilterUi(
                tabStateManager = tabStateManager,
                epaStateManager = epaStateManager,
                backgroundDispatcher = backgroundDispatcher
            )
        }

        EpaViewUpperState.Layout -> PanelMenu(
            title = "Layout Settings",
            modifier = Modifier.padding(8.dp),
            onClose = { onClose() }
        ) {
            LayoutUi(
                tabStateManager = tabStateManager,
            )
        }

        EpaViewUpperState.Analysis -> PanelMenu(
            title = "Analysis",
            modifier = Modifier.Companion.padding(8.dp),
            onClose = { onClose() }
        ) {
            AnalysisUi(
                tabStateManager = tabStateManager,
                epaStateManager = epaStateManager
            )
        }

        EpaViewUpperState.NaturalLanguage -> PanelMenu(
            title = "NLI",
            modifier = Modifier.Companion.padding(8.dp),
            onClose = { onClose() }
        ) {
            InfoText("Not implemented")
        }

        EpaViewUpperState.None -> { /* Should not happen */
        }
    }
}

@Composable
fun AnalysisUi(
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

    if (currentTab?.selectedState != null && currentEpa != null) {
        StateInfo(
            selectedState = currentTab.selectedState,
            extendedPrefixAutomaton = currentEpa,
        ) {
            tabStateManager.setSelectedStateForCurrentTab(it)
        }
    }else {
        CircularProgressIndicatorBig()
    }
}