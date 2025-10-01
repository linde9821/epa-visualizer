package moritz.lindner.masterarbeit.ui.components.epaview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.animation.AnimationUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.filter.FilterUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.layout.LayoutUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.project.ProjectOverviewUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.statistics.StatisticsComparisonUi
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
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.HorizontalSplitLayout
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.InfoText
import org.jetbrains.jewel.ui.component.SplitLayoutState
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticalSplitLayout
import org.jetbrains.jewel.ui.component.rememberSplitLayoutState
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
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

    var upperState: EpaViewUpperState by remember { mutableStateOf(None) }
    var lowerState: EpaViewLowerState by remember { mutableStateOf(EpaViewLowerState.None) }

    LaunchedEffect(lowerState) {
        if (lowerState == EpaViewLowerState.None) {
            verticalSplitState.dividerPosition = 1f
        } else {
            verticalSplitState.dividerPosition = 0.7f
        }
    }

    LaunchedEffect(upperState) {
        if (upperState == None) {
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
                ) {
                    upperState = None
                }
            },
            second = {
                LowerLayout(
                    lowerState,
                    tabsStateManager,
                    epaStateManager,
                    backgroundDispatcher
                )
            },
            modifier = Modifier.fillMaxWidth(),
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
        EpaViewLowerState.Animation -> {
            AnimationUi(
                epaStateManager = epaStateManager,
                tabStateManager = tabStateManager,
                backgroundDispatcher = backgroundDispatcher
            )
        }

        EpaViewLowerState.Statistics -> {
            StatisticsComparisonUi(
                tabStateManager,
                epaStateManager,
            )
        }

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
        modifier = Modifier.fillMaxWidth(),
        firstPaneMinWidth = 0.dp,
        secondPaneMinWidth = 300.dp,
    )
}

@Composable
fun SidePanelMenu(
    title: String,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(2.dp),
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(title, style = JewelTheme.typography.h1TextStyle)
            IconButton(
                onClick = onClose,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(AllIconsKeys.General.ChevronLeft, "Chevron")
            }
        }
        Divider(
            orientation = Orientation.Horizontal,
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = JewelTheme.contentColor.copy(alpha = 0.2f)
        )
        content()
    }
}

@Composable
private fun SidePanelContent(
    upperState: EpaViewUpperState,
    projectState: ProjectStateManager,
    tabStateManager: TabStateManager,
    epaStateManager: EpaStateManager,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit
) {
    when (upperState) {
        EpaViewUpperState.Project -> SidePanelMenu(
            "Project Settings",
            modifier = Modifier.padding(8.dp),
            onClose = {onClose()}
        ) {
            ProjectOverviewUi(projectState)
        }

        Filter -> SidePanelMenu(
            "Filter Settings",
            modifier = Modifier.padding(8.dp),
            onClose = {onClose()}
        ) {
            FilterUi(
                tabStateManager = tabStateManager,
                epaStateManager = epaStateManager,
                backgroundDispatcher = backgroundDispatcher
            )
        }

        Layout -> SidePanelMenu(
            title = "Layout Settings",
            modifier = Modifier.padding(8.dp),
            onClose = {onClose()}
        ) {
            LayoutUi(
                tabStateManager = tabStateManager,
            )
        }

        Analysis -> SidePanelMenu(
            title = "Analysis",
            modifier = Modifier.padding(8.dp),
            onClose = {onClose()}
        ) {
            InfoText("Not implemented")
        }

        NaturalLanguage -> SidePanelMenu(
            title = "NLI",
            modifier = Modifier.padding(8.dp),
            onClose = {onClose()}
        ) {
            InfoText("Not implemented")
        }

        None -> { /* Should not happen */
        }
    }
}