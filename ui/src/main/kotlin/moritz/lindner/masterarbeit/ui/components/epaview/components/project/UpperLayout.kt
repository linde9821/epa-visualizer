package moritz.lindner.masterarbeit.ui.components.epaview.components.project

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.epaview.components.tabs.TabsComponent
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.ProjectStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager
import org.jetbrains.jewel.ui.component.HorizontalSplitLayout
import org.jetbrains.jewel.ui.component.SplitLayoutState

@Composable
fun UpperLayout(
    upperState: EpaViewUpperState,
    horizontalSplitState: SplitLayoutState,
    projectState: ProjectStateManager,
    tabStateManager: TabStateManager,
    epaStateManager: EpaStateManager,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit
) {
    HorizontalSplitLayout(
        state = horizontalSplitState,
        first = {
            SidePanelContent(
                upperState = upperState,
                projectState = projectState,
                tabStateManager = tabStateManager,
                epaStateManager = epaStateManager,
                backgroundDispatcher = backgroundDispatcher,
                onClose = onClose
            )
        },
        second = {
            TabsComponent(
                tabStateManager = tabStateManager,
                epaStateManager = epaStateManager,
                backgroundDispatcher = backgroundDispatcher,
            )
        },
        modifier = Modifier.Companion.fillMaxWidth(),
        firstPaneMinWidth = 0.dp,
        secondPaneMinWidth = 300.dp,
    )
}