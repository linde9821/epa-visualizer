package moritz.lindner.masterarbeit.ui.components.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EventLogMapper
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography
import java.io.File

@Composable
fun CreateProjectStep(
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
        Text("Review and create project", style = JewelTheme.Companion.typography.h3TextStyle)

        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .border(1.dp, JewelTheme.Companion.contentColor.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.Companion.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Project Summary", style = JewelTheme.Companion.typography.h4TextStyle)

                Text("Name: $projectName", style = JewelTheme.Companion.typography.regular)
                if (projectDescription.isNotBlank()) {
                    Text("Description: $projectDescription", style = JewelTheme.Companion.typography.regular)
                }
                Text("XES File: ${xesFile?.name}", style = JewelTheme.Companion.typography.regular)
                Text("Mapper: ${selectedMapper?.name}", style = JewelTheme.Companion.typography.regular)
                Text("Location: ${projectFolder?.absolutePath}", style = JewelTheme.Companion.typography.regular)
                Text(
                    "Final Path: ${projectFolder?.resolve(projectName)?.absolutePath}",
                    style = JewelTheme.Companion.typography.regular
                )
            }
        }

        errorMessage?.let { error ->
            Box(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .border(1.dp, Color.Companion.Red)
                    .background(Color.Companion.Red.copy(alpha = 0.1f))
                    .padding(16.dp)
            ) {
                Text(
                    text = error,
                    color = Color.Companion.Red,
                    style = JewelTheme.Companion.typography.regular
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
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.Companion.size(16.dp),
                        )
                    }
                    Text(if (isCreating) "Creating..." else "Create Project")
                }
            }
        }
    }
}