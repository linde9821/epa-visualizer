@file:JvmName("ProjectOverviewUiKt")

package moritz.lindner.masterarbeit.ui.components.epaview.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.project.Project
import moritz.lindner.masterarbeit.ui.components.epaview.components.tabs.TabsComponent
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.ToolbarUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.filter.FilterUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.layout.LayoutUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.project.ProjectOverviewUi
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewLowerState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.Analysis
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.Filter
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.Layout
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.NaturalLanguage
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewUpperState.None
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.ProjectStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.HorizontalSplitLayout
import org.jetbrains.jewel.ui.component.SplitLayoutState
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticalSplitLayout
import org.jetbrains.jewel.ui.component.rememberSplitLayoutState
import java.util.UUID

@Composable
fun ProjectUi(
    project: Project,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    val projectStateManager = remember { ProjectStateManager(project, backgroundDispatcher) }
    val tabsStateManager = remember { TabStateManager() }
    val epaStateManager = remember {
        EpaStateManager(
            tabStateManager = tabsStateManager,
            projectStateManager = projectStateManager,
            backgroundDispatcher = backgroundDispatcher,
        )
    }

    LaunchedEffect(Unit) {
        tabsStateManager.addTab(
            id = UUID.randomUUID().toString(),
            title = "root",
            filters = emptyList(),
            layoutConfig = LayoutConfig.RadialWalker()
        )
    }

    val horizontalSplitState = rememberSplitLayoutState(0.3f)
    val verticalSplitState = rememberSplitLayoutState(0.7f)

    var upperState: EpaViewUpperState by remember { mutableStateOf(EpaViewUpperState.None) }
    var lowerState: EpaViewLowerState by remember { mutableStateOf(EpaViewLowerState.None) }

    LaunchedEffect(lowerState) {
        if (lowerState == EpaViewLowerState.None) {
            verticalSplitState.dividerPosition = 1f
        } else {
            verticalSplitState.dividerPosition = 0.7f
        }
    }

    LaunchedEffect(upperState) {
        if (upperState == EpaViewUpperState.None) {
            horizontalSplitState.dividerPosition = 0.0f
        } else {
            horizontalSplitState.dividerPosition = 0.3f
        }
    }

    Row {
        ToolbarUi(
            upperState = upperState,
            onUpperStateChange = { upperState = it },
            lowerState = lowerState,
            onLowerStateChange = { lowerState = it },
            onClose = onClose,
        )

        VerticalSplitLayout(
            state = verticalSplitState,
            first = {
                UpperLayout(
                    upperState,
                    horizontalSplitState,
                    projectStateManager,
                    tabsStateManager,
                    epaStateManager,
                    backgroundDispatcher
                )
            },
            second = {
                LowerLayout(
                    lowerState,
                    tabsStateManager,
                    epaStateManager,
                    backgroundDispatcher
                )
            },
            modifier = Modifier.fillMaxWidth()
                .border(4.dp, color = JewelTheme.globalColors.borders.focused),
            firstPaneMinWidth = 300.dp,
            secondPaneMinWidth = 0.dp,
        )
    }
}

@Composable
fun LowerLayout(
    lowerState: EpaViewLowerState,
    tabStateManager: TabStateManager,
    epaStateManager: EpaStateManager,
    backgroundDispatcher: ExecutorCoroutineDispatcher
) {
    when (lowerState) {
        EpaViewLowerState.Animation -> TODO()
        EpaViewLowerState.Statistics -> TODO()
        EpaViewLowerState.None -> {

        }
    }
}

@Composable
private fun UpperLayout(
    upperState: EpaViewUpperState,
    horizontalSplitState: SplitLayoutState,
    projectState: ProjectStateManager,
    tabStateManager: TabStateManager,
    epaStateManager: EpaStateManager,
    backgroundDispatcher: ExecutorCoroutineDispatcher
) {
    HorizontalSplitLayout(
        state = horizontalSplitState,
        first = {
            SidePanelContent(
                upperState = upperState,
                projectState = projectState,
                tabStateManager = tabStateManager,
                epaStateManager = epaStateManager,
                backgroundDispatcher = backgroundDispatcher
            )
        },
        second = {
            TabsComponent(
                tabStateManager = tabStateManager,
                epaStateManager = epaStateManager,
                backgroundDispatcher = backgroundDispatcher,
            )
        },
        modifier = Modifier.fillMaxWidth().border(4.dp, color = JewelTheme.globalColors.borders.normal),
        firstPaneMinWidth = 0.dp,
        secondPaneMinWidth = 300.dp,
    )
}

@Composable
private fun SidePanelContent(
    upperState: EpaViewUpperState,
    projectState: ProjectStateManager,
    tabStateManager: TabStateManager,
    epaStateManager: EpaStateManager,
    backgroundDispatcher: ExecutorCoroutineDispatcher
) {
    when (upperState) {
        Filter -> FilterUi(
            tabStateManager = tabStateManager,
            epaStateManager = epaStateManager,
            backgroundDispatcher = backgroundDispatcher,
        )

        Layout -> LayoutUi(
            tabStateManager = tabStateManager,
        )

        EpaViewUpperState.Project -> ProjectOverviewUi(projectState)
        Analysis -> {
            Text("Analysis not implemented")
        }

        NaturalLanguage -> {
            Text("Natural language not implemented")
        }

        None -> { /* Should not happen */
        }
    }
}