package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2018ChallengeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EventLogMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.project.Project
import moritz.lindner.masterarbeit.ui.components.epaview.components.EpaTreeViewUi
import moritz.lindner.masterarbeit.ui.components.fileselection.ProjectSelectionUi
import moritz.lindner.masterarbeit.ui.components.loadingepa.ConstructEpaUi
import moritz.lindner.masterarbeit.ui.logger
import moritz.lindner.masterarbeit.ui.state.ApplicationState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.SegmentedControl
import org.jetbrains.jewel.ui.component.SegmentedControlButtonData
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
import java.io.File

@Composable
fun EPAVisualizerUi(backgroundDispatcher: ExecutorCoroutineDispatcher) {
    var state: ApplicationState by remember { mutableStateOf(ApplicationState.Start()) }
    val scope = rememberCoroutineScope()

    Column {
        when (val currentState = state) {
            is ApplicationState.Start -> ProjectSelectionUi(
                error = currentState.constructionError,
                onProjectOpen = {
                    state = ApplicationState.ProjectSelected(it)
                },
                onNewProject = {
                    state = ApplicationState.NewProject
                }
            )

            is ApplicationState.NewProject -> NewProjectUi(
                onAbort = { state = ApplicationState.Start() },
                onProjectCreated = { state = ApplicationState.ProjectSelected(it) }
            )

            is ApplicationState.ProjectSelected -> {
                ConstructEpaUi(scope, backgroundDispatcher, currentState.project, { epa ->
                    state = ApplicationState.EpaConstructed(epa)
                }, {
                    state = ApplicationState.Start()
                }) { error, e ->
                    logger.error(e) { error }
                    state = ApplicationState.Start(error)
                }
            }

            is ApplicationState.EpaConstructed ->
                EpaTreeViewUi(
                    currentState.extendedPrefixAutomaton,
                    backgroundDispatcher,
                    onClose = {
                        state = ApplicationState.Start()
                    },
                )
        }
    }
}

@Composable
fun NewProjectUi(onAbort: () -> Unit, onProjectCreated: (Project) -> Unit) {
    val steps = listOf("Project Info", "Select XES", "Select Mapper", "Choose Location", "Create")
    var selectedStepIndex by remember { mutableStateOf(0) }

    // Project creation state
    var projectName by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }
    var selectedXesFile by remember { mutableStateOf<File?>(null) }
    var selectedMapper by remember { mutableStateOf<EventLogMapper<Long>?>(null) }
    var selectedProjectFolder by remember { mutableStateOf<File?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Available mappers
    val mappers = listOf(
        SampleEventMapper(),
        BPI2017OfferChallengeEventMapper(),
        BPI2017ChallengeEventMapper(),
        BPI2018ChallengeMapper(),
    )

    val xesFileLauncher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("xes", "gz"))
    ) { file ->
        file?.let { selectedXesFile = File(it.file.absolutePath) }
    }

    val projectFolderLauncher = rememberDirectoryPickerLauncher { directory ->
        directory?.let { selectedProjectFolder = File(it.file.absolutePath) }
    }

    val buttons = remember(selectedStepIndex) {
        steps.mapIndexed { index, stepName ->
            SegmentedControlButtonData(
                selected = index == selectedStepIndex,
                content = { _ -> Text(stepName) },
                onSelect = {
                    // Only allow going to previous steps or current step
                    if (index <= selectedStepIndex) {
                        selectedStepIndex = index
                    }
                },
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create New Project",
            style = JewelTheme.typography.h1TextStyle
        )

        SegmentedControl(buttons = buttons)

        Spacer(modifier = Modifier.height(16.dp))

        // Step content
        when (selectedStepIndex) {
            0 -> ProjectInfoStep(
                projectName = projectName,
                onProjectNameChange = { projectName = it },
                onNext = { if (projectName.isNotBlank()) selectedStepIndex = 1 }
            )

            1 -> SelectXesStep(
                selectedFile = selectedXesFile,
                onFileSelect = { xesFileLauncher.launch() },
                onNext = { if (selectedXesFile != null) selectedStepIndex = 2 },
                onPrevious = { selectedStepIndex = 0 }
            )

            2 -> ChooseMapperStep(
                mappers = mappers,
                selectedMapper = selectedMapper,
                onMapperSelect = { selectedMapper = it },
                onNext = { if (selectedMapper != null) selectedStepIndex = 3 },
                onPrevious = { selectedStepIndex = 1 }
            )

            3 -> ChooseLocationStep(
                selectedFolder = selectedProjectFolder,
                onFolderSelect = { projectFolderLauncher.launch() },
                onNext = { if (selectedProjectFolder != null) selectedStepIndex = 4 },
                onPrevious = { selectedStepIndex = 2 }
            )

            4 -> CreateProjectStep(
                projectName = projectName,
                projectDescription = projectDescription,
                xesFile = selectedXesFile,
                projectFolder = selectedProjectFolder,
                isCreating = isCreating,
                errorMessage = errorMessage,
                onCreate = {
                    if (!isCreating && selectedXesFile != null && selectedProjectFolder != null && selectedMapper != null) {
                        isCreating = true
                        errorMessage = null

                        try {
                            val finalProjectFolder = selectedProjectFolder!!.resolve(projectName)
                            val project = Project.create(
                                name = projectName,
                                projectFolder = finalProjectFolder.absolutePath,
                                xesFilePath = selectedXesFile!!.absolutePath,
                                mapper = selectedMapper!!
                            )
                            onProjectCreated(project)
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to create project" }
                            errorMessage = "Failed to create project: ${e.message}"
                            isCreating = false
                        }
                    }
                },
                onPrevious = { selectedStepIndex = 3 },
                selectedMapper = selectedMapper
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onAbort) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun ProjectInfoStep(
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val state = rememberTextFieldState(projectName)

    LaunchedEffect(state.text) {
        onProjectNameChange(state.text.toString())
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Enter project name", style = JewelTheme.typography.h3TextStyle)

        TextField(
            state = state,
            enabled = true,
            readOnly = false,
        )

        DefaultButton(
            onClick = onNext,
            enabled = projectName.isNotBlank()
        ) {
            Text("Next")
        }
    }
}

@Composable
private fun SelectXesStep(
    selectedFile: File?,
    onFileSelect: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Select XES event log file", style = JewelTheme.typography.h3TextStyle)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JewelTheme.contentColor.copy(alpha = 0.3f))
                .clickable { onFileSelect() }
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    key = AllIconsKeys.Actions.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = JewelTheme.contentColor
                )
                Text(
                    text = selectedFile?.name ?: "Click to select XES file",
                    style = JewelTheme.typography.regular
                )
                selectedFile?.let {
                    Text(
                        text = it.absolutePath,
                        style = JewelTheme.typography.regular.copy(fontSize = 12.sp),
                        color = JewelTheme.contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPrevious) {
                Text("Previous")
            }
            DefaultButton(
                onClick = onNext,
                enabled = selectedFile != null
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun ChooseLocationStep(
    selectedFolder: File?,
    onFolderSelect: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Choose project location", style = JewelTheme.typography.h3TextStyle)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JewelTheme.contentColor.copy(alpha = 0.3f))
                .clickable { onFolderSelect() }
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    key = AllIconsKeys.Nodes.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = JewelTheme.contentColor
                )
                Text(
                    text = selectedFolder?.name ?: "Click to select folder",
                    style = JewelTheme.typography.regular
                )
                selectedFolder?.let {
                    Text(
                        text = it.absolutePath,
                        style = JewelTheme.typography.regular.copy(fontSize = 12.sp),
                        color = JewelTheme.contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPrevious) {
                Text("Previous")
            }
            DefaultButton(
                onClick = onNext,
                enabled = selectedFolder != null
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun ChooseMapperStep(
    mappers: List<EventLogMapper<Long>>,
    selectedMapper: EventLogMapper<Long>?,
    onMapperSelect: (EventLogMapper<Long>) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    // Update selected mapper when index changes
    LaunchedEffect(selectedIndex) {
        if (selectedIndex < mappers.size) {
            onMapperSelect(mappers[selectedIndex])
        }
    }

    // Initialize with first mapper if none selected
    LaunchedEffect(Unit) {
        if (selectedMapper == null && mappers.isNotEmpty()) {
            onMapperSelect(mappers[0])
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Select Event Log Mapper", style = JewelTheme.typography.h3TextStyle)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JewelTheme.contentColor.copy(alpha = 0.3f))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        key = AllIconsKeys.Actions.MoveToButton,
                        contentDescription = null,
                        tint = JewelTheme.contentColor
                    )
                    Text(
                        "Select Event Log Mapper:",
                        style = JewelTheme.typography.regular
                    )
                }

                ListComboBox(
                    items = mappers.map { it.name },
                    selectedIndex = selectedIndex,
                    onSelectedItemChange = { selectedIndex = it },
                    modifier = Modifier.width(250.dp)
                )

                selectedMapper?.let { mapper ->
                    Text(
                        text = "Selected: ${mapper.name}",
                        style = JewelTheme.typography.regular.copy(fontSize = 12.sp),
                        color = JewelTheme.contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPrevious) {
                Text("Previous")
            }
            DefaultButton(
                onClick = onNext,
                enabled = selectedMapper != null
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun CreateProjectStep(
    projectName: String,
    projectDescription: String,
    xesFile: File?,
    selectedMapper: EventLogMapper<Long>?,
    projectFolder: File?,
    isCreating: Boolean,
    errorMessage: String?,
    onCreate: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Review and create project", style = JewelTheme.typography.h3TextStyle)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JewelTheme.contentColor.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Project Summary", style = JewelTheme.typography.h4TextStyle)

                Text("Name: $projectName", style = JewelTheme.typography.regular)
                if (projectDescription.isNotBlank()) {
                    Text("Description: $projectDescription", style = JewelTheme.typography.regular)
                }
                Text("XES File: ${xesFile?.name}", style = JewelTheme.typography.regular)
                Text("Mapper: ${selectedMapper?.name}", style = JewelTheme.typography.regular)
                Text("Location: ${projectFolder?.absolutePath}", style = JewelTheme.typography.regular)
                Text("Final Path: ${projectFolder?.resolve(projectName)?.absolutePath}", style = JewelTheme.typography.regular)
            }
        }

        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Red)
                    .background(Color.Red.copy(alpha = 0.1f))
                    .padding(16.dp)
            ) {
                Text(
                    text = error,
                    color = Color.Red,
                    style = JewelTheme.typography.regular
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = !isCreating
            ) {
                Text("Previous")
            }
            DefaultButton(
                onClick = onCreate,
                enabled = !isCreating && xesFile != null && selectedMapper != null && projectFolder != null
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(if (isCreating) "Creating..." else "Create Project")
                }
            }
        }
    }
}