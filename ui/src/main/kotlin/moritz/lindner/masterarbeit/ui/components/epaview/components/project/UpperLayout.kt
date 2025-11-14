package moritz.lindner.masterarbeit.ui.components.epaview.components.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.epaview.components.tabs.TabsComponent
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.ProjectStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager
import org.jetbrains.jewel.ui.component.HorizontalSplitLayout
import org.jetbrains.jewel.ui.component.SplitLayoutState
import org.jetbrains.jewel.ui.component.rememberSplitLayoutState

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
            MultiSplitLayout(
                panels = listOf(

                    {
                        TabsComponent(
                            tabStateManager = tabStateManager,
                            epaStateManager = epaStateManager,
                        )
                    }
                )

            )
        },
        modifier = Modifier.fillMaxWidth(),
        firstPaneMinWidth = 0.dp,
        secondPaneMinWidth = 300.dp,
    )
}

@Composable
fun MultiSplitLayout(
    panels: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    minPaneWidth: Dp = 200.dp,
) {
    when {
        panels.isEmpty() -> {
            // No panels to show
        }

        panels.size == 1 -> {
            Box(modifier = modifier) {
                panels[0]()
            }
        }

        else -> {
            val splitState = rememberSplitLayoutState(1f / panels.size)

            HorizontalSplitLayout(
                state = splitState,
                first = {
                    panels[0]()
                },
                second = {
                    // Recursively render the remaining panels
                    MultiSplitLayout(
                        panels = panels.drop(1),
                        minPaneWidth = minPaneWidth
                    )
                },
                modifier = modifier,
                firstPaneMinWidth = minPaneWidth,
                secondPaneMinWidth = minPaneWidth,
            )
        }
    }
}