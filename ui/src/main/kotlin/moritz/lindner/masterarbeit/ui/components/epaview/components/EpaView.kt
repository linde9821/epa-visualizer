package moritz.lindner.masterarbeit.ui.components.epaview.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.api.LayoutService
import moritz.lindner.masterarbeit.epa.construction.builder.xes.Mappers
import moritz.lindner.masterarbeit.epa.project.Project
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewStateLower
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewStateUpper
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewStateUpper.*
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.HorizontalSplitLayout
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.SplitLayoutState
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticalSplitLayout
import org.jetbrains.jewel.ui.component.rememberSplitLayoutState
import org.jetbrains.jewel.ui.typography

class ProjectViewModel(
    project: Project,
    private val backgroundDispatcher: ExecutorCoroutineDispatcher,
) {
    private val _projectState = MutableStateFlow(project)
    val projectState: StateFlow<Project> = _projectState.asStateFlow()

    private val epaService = EpaService<Long>()
    private val layoutService = LayoutService<Long>()

    fun updateProject(project: Project) {
        project.saveMetadata()
        _projectState.value = project
    }
}

@Composable
fun LayoutTest(
    project: Project,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {

    val viewModel by remember {
        mutableStateOf(
            ProjectViewModel(
                project = project,
                backgroundDispatcher = backgroundDispatcher,
            ),
        )
    }

    val horizontalSplitState = rememberSplitLayoutState(0.3f)
    val verticalSplitState = rememberSplitLayoutState(0.7f)

    var upperState: EpaViewStateUpper by remember { mutableStateOf(EpaViewStateUpper.Project) }
    var lowerState: EpaViewStateLower by remember { mutableStateOf(EpaViewStateLower.None) }

    Row {
        // Toolbar
        TabsUi(
            upperState = upperState,
            onUpperStateChange = { upperState = it },
            lowerState = lowerState,
            onLowerStateChange = { lowerState = it },
            onClose = onClose,
        )

        when (lowerState) {
            EpaViewStateLower.Animation, EpaViewStateLower.Statistics -> {
                VerticalSplitLayout(
                    state = verticalSplitState,
                    first = {
                        UpperLayout(upperState, horizontalSplitState, viewModel)
                    },
                    second = {
                        Text("Lower + $lowerState")
                    },
                    modifier = Modifier.fillMaxWidth().border(4.dp, color = JewelTheme.globalColors.borders.normal),
                    firstPaneMinWidth = 300.dp,
                    secondPaneMinWidth = 0.dp,
                )
            }

            EpaViewStateLower.None -> {
                UpperLayout(upperState, horizontalSplitState, viewModel)
            }
        }
    }
}

@Composable
private fun ProjectUi(
    viewModel: ProjectViewModel
) {
    val mappers = Mappers.getMappers()
    val project by viewModel.projectState.collectAsState()

    var selectedIndex by remember(project.mapperName) {
        mutableIntStateOf(
            mappers.map { it.name }.indexOf(project.mapperName)
        )
    }

    Column {
        Text("Project: ${project.name}")
        Text("Created on ${project.createdAt}")

        ListComboBox(
            items = mappers.map { it.name },
            selectedIndex = selectedIndex,
            onSelectedItemChange = { selectedIndex = it },
            modifier = Modifier.width(250.dp)
        )

        DefaultButton(
            onClick = {
                val updatedProject = project.withMapper(mappers[selectedIndex])
                viewModel.updateProject(updatedProject)
            },
        ) {
            Text("Update")
        }
    }
}

@Composable
private fun UpperLayout(
    upperState: EpaViewStateUpper,
    horizontalSplitState: SplitLayoutState,
    viewModel: ProjectViewModel
) {

    when (upperState) {
        Filter, Layout, EpaViewStateUpper.Project, Analysis, NaturalLanguage -> {
            HorizontalSplitLayout(
                state = horizontalSplitState,
                first = {
                    when(upperState){
                        Analysis -> TODO()
                        Filter -> TODO()
                        Layout -> TODO()
                        NaturalLanguage -> TODO()
                        EpaViewStateUpper.Project -> {
                            ProjectUi(viewModel)
                        }

                        else -> {}
                    }
                },
                second = { Text("Right") },
                modifier = Modifier.fillMaxWidth().border(4.dp, color = JewelTheme.globalColors.borders.normal),
                firstPaneMinWidth = 0.dp,
                secondPaneMinWidth = 300.dp,
            )
        }

        None -> {
            Text("Right")
        }
    }
}

//@Composable
//fun EpaTreeViewUi(
//    epa: ExtendedPrefixAutomaton<Long>,
//    backgroundDispatcher: ExecutorCoroutineDispatcher,
//    onClose: () -> Unit,
//) {
//    val epaViewModel by remember {
//        mutableStateOf(
//            EpaViewModel(
//                completeEpa = epa,
//                backgroundDispatcher = backgroundDispatcher,
//            ),
//        )
//    }
//
//    val epaUiState by epaViewModel.epaUiState.collectAsState()
//    val statisticsState by epaViewModel.statistics.collectAsState()
//    val animationState by epaViewModel.animationState.collectAsState()
//
//    var upperState: EpaViewStateUpper by remember { mutableStateOf(EpaViewStateUpper.None) }
//    var lowerState: EpaViewStateLower by remember { mutableStateOf(EpaViewStateLower.None) }
//
//    Row(
//        modifier = Modifier.fillMaxSize(),
//    ) {
//        TabsUi(
//            upperState = upperState,
//            onUpperStateChange = { upperState = it },
//            lowerState = lowerState,
//            onLowerStateChange = { lowerState = it },
//            onClose = onClose,
//        )
//
//        // OTHER
//        Column(
//            modifier = Modifier.fillMaxSize(),
//        ) {
//            Row(
//                modifier =
//                    Modifier
//                        .fillMaxWidth()
//                        .weight(if (lowerState != EpaViewStateUpper.None) 3f else 1f),
//            ) {
//                // UPPER
//                when (upperState) {
//                    EpaViewStateUpper.Filter -> {
//                        FilterUi(
//                            epa = epa,
//                            epaUiState = epaUiState,
//                            epaViewModel = epaViewModel,
//                            backgroundDispatcher = backgroundDispatcher,
//                            modifier =
//                                Modifier
//                                    .weight(1f)
//                                    .fillMaxHeight(),
//                        )
//                        Divider(
//                            orientation = Orientation.Vertical,
//                            modifier = Modifier.fillMaxHeight(),
//                            thickness = 1.dp,
//                            color = JewelTheme.contentColor.copy(alpha = 0.2f)
//                        )
//                    }
//
//                    EpaViewStateUpper.Layout -> {
//                        LayoutUi(
//                            modifier =
//                                Modifier
//                                    .weight(1f)
//                                    .fillMaxHeight(),
//                        ) {
//                            epaViewModel.updateLayout(it)
//                        }
//                        Divider(
//                            orientation = Orientation.Vertical,
//                            modifier = Modifier.fillMaxHeight(),
//                            thickness = 1.dp,
//                            color = JewelTheme.contentColor.copy(alpha = 0.2f)
//                        )
//                    }
//
//                    EpaViewStateUpper.None -> null
//                }
//
//                TidyTreeUi(
//                    epaUiState,
//                    animationState,
//                    backgroundDispatcher,
//                    modifier =
//                        Modifier
//                            .weight(if (upperState != EpaViewStateUpper.None || lowerState != EpaViewStateLower.None) 2f else 1f)
//                            .fillMaxHeight(),
//                )
//            }
//
//            // LOWER
//            if (lowerState != EpaViewStateLower.None) {
//                Divider(
//                    orientation = Orientation.Horizontal,
//                    modifier = Modifier.fillMaxWidth(),
//                    thickness = 1.dp,
//                    color = JewelTheme.contentColor.copy(alpha = 0.2f)
//                )
//
//                Row(
//                    modifier =
//                        Modifier
//                            .fillMaxWidth()
//                            .weight(1f),
//                ) {
//                    when (lowerState) {
//                        EpaViewStateLower.Animation -> {
//                            AnimationUi(epaUiState.filteredEpa, epaViewModel, backgroundDispatcher)
//                        }
//
//                        EpaViewStateLower.Statistics -> {
//                            StatisticsComparisonUi(statisticsState)
//                        }
//
//                        EpaViewStateLower.None -> null
//                    }
//                }
//            }
//        }
//    }
//}
