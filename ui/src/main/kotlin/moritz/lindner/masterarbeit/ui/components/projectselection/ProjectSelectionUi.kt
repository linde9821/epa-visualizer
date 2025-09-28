package moritz.lindner.masterarbeit.ui.components.projectselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import moritz.lindner.masterarbeit.epa.project.Project
import moritz.lindner.masterarbeit.ui.common.Constants.APPLICATION_NAME
import moritz.lindner.masterarbeit.ui.common.Icons
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.InlineErrorBanner
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
import kotlin.io.path.Path

@Composable
fun ProjectSelectionUi(
    error: String?,
    onProjectOpen: (directory: Project) -> Unit,
    onNewProject: () -> Unit,
    onError: (String) -> Unit,
) {

    val openProjectLauncher = rememberDirectoryPickerLauncher { directory ->
        directory?.let { projectPath ->
            try {
                val project = Project.loadFromFolder(Path(projectPath.file.absolutePath))
                onProjectOpen(project)
            } catch (e: Exception) {
                logger.error(e) { "Couldn't load project" }
                onError("Couldn't load project: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.logo,
            contentDescription = "App Logo",
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to $APPLICATION_NAME",
            style = JewelTheme.typography.h1TextStyle
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Open an existing project or create a new one",
            style = JewelTheme.typography.regular
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            InlineErrorBanner(
                text = error,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }


        DefaultButton(
            onClick = { onNewProject() },
            modifier = Modifier.fillMaxWidth(0.2f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    key = AllIconsKeys.Actions.NewFolder,
                    contentDescription = null,
                    tint = JewelTheme.contentColor,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("New Project", style = JewelTheme.typography.regular)
            }
        }


        Spacer(modifier = Modifier.height(12.dp))

        DefaultButton(
            onClick = {
                openProjectLauncher.launch()
            },
            modifier = Modifier.fillMaxWidth(0.2f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    key = AllIconsKeys.Actions.ProjectDirectory,
                    contentDescription = null,
                    tint = JewelTheme.contentColor,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Open Project", style = JewelTheme.typography.regular)
            }
        }
    }
}
