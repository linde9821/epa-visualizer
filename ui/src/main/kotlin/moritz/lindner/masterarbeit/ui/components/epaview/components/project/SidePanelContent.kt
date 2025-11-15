package moritz.lindner.masterarbeit.ui.components.epaview.components.project

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.DetailsUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.filter.FilterUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.layout.LayoutUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.project.ProjectOverviewUi
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.*
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.ProjectStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager

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
        Project -> PanelMenu(
            "Project Settings",
            modifier = Modifier.padding(8.dp),
            onClose = { onClose() }
        ) {
            ProjectOverviewUi(projectState)
        }

        Filter -> PanelMenu(
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

        Layout -> PanelMenu(
            title = "Layout Settings",
            modifier = Modifier.padding(8.dp),
            onClose = { onClose() }
        ) {
            LayoutUi(
                tabStateManager = tabStateManager,
                epaStateManager = epaStateManager
            )
        }

        Details -> PanelMenu(
            title = "Details",
            modifier = Modifier.Companion.padding(8.dp),
            onClose = { onClose() }
        ) {
            DetailsUi(
                tabStateManager = tabStateManager,
                epaStateManager = epaStateManager
            )
        }

        None -> {
            /* Should not happen */
        }
    }
}

