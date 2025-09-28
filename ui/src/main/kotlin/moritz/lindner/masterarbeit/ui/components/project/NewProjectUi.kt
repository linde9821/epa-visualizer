package moritz.lindner.masterarbeit.ui.components.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2018ChallengeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EventLogMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import moritz.lindner.masterarbeit.epa.project.Project
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.SegmentedControl
import org.jetbrains.jewel.ui.component.SegmentedControlButtonData
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.painter.badge.DotBadgeShape
import org.jetbrains.jewel.ui.painter.hints.Badge
import org.jetbrains.jewel.ui.typography
import java.io.File
import kotlin.math.max

@Composable
fun NewProjectUi(onAbort: () -> Unit, onProjectCreated: (Project) -> Unit) {
    val steps = listOf("Project Info", "Select XES", "Select Mapper", "Choose Location", "Create")
    var selectedStepIndex by remember { mutableStateOf(0) }
    var currentMax by remember { mutableStateOf(0) }

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
                content = { _ ->
                    val color = if (index >= currentMax) Color.Red else Color.Green
                    Row{
                        Text(stepName)
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            key = AllIconsKeys.General.Settings,
                            contentDescription = "",
                            hint =  Badge(color, DotBadgeShape.Default),
                        )
                    }
                },
                onSelect = {
                    // Only allow going to previous steps or current step
                    if (index <= selectedStepIndex || index <= currentMax && projectName.isNotBlank()) {
                        selectedStepIndex = index
                    }
                },
            )
        }
    }

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create New Project",
            style = JewelTheme.Companion.typography.h1TextStyle
        )

        SegmentedControl(buttons = buttons)

        Spacer(modifier = Modifier.Companion.height(16.dp))

        // Step content
        when (selectedStepIndex) {
            0 -> ProjectInfoStep(
                projectName = projectName,
                onProjectNameChange = { projectName = it },
                onNext = {
                    if (projectName.isNotBlank()) selectedStepIndex = 1
                    currentMax = max(currentMax, selectedStepIndex)
                }
            )

            1 -> SelectXesStep(
                selectedFile = selectedXesFile,
                onFileSelect = { selectedFile ->
                    if (selectedFile == null) xesFileLauncher.launch()
                    else selectedXesFile = File(selectedFile.absolutePath)
                },
                onNext = {
                    if (selectedXesFile != null) selectedStepIndex = 2
                    currentMax = max(currentMax, selectedStepIndex)
                },
                onPrevious = { selectedStepIndex = 0 }
            )

            2 -> ChooseMapperStep(
                mappers = mappers,
                selectedMapper = selectedMapper,
                onMapperSelect = { selectedMapper = it },
                onNext = {
                    if (selectedMapper != null) selectedStepIndex = 3
                    currentMax = max(currentMax, selectedStepIndex)
                },
                onPrevious = { selectedStepIndex = 1 }
            )

            3 -> ChooseLocationStep(
                selectedFolder = selectedProjectFolder,
                onFolderSelect = { projectFolderLauncher.launch() },
                onNext = {
                    if (selectedProjectFolder != null) selectedStepIndex = 4
                    currentMax = max(currentMax, selectedStepIndex)
                },
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
                            val project = Project.Companion.create(
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

        Spacer(modifier = Modifier.Companion.weight(1f))

        // Bottom buttons
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onAbort) {
                Text("Cancel")
            }
        }
    }
}