package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.construction.builder.xes.Mappers
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.ProjectStateManager
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Text

@OptIn(ExperimentalJewelApi::class)
@Composable
fun ProjectOverviewUi(
    projectStateManager: ProjectStateManager
) {
    val mappers = Mappers.getMappers()
    val project by projectStateManager.project.collectAsState()

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
            enabled = mappers[selectedIndex] != project.getMapper(),
            onClick = {
                val updatedProject = project.withMapper(mappers[selectedIndex])
                projectStateManager.updateProject(updatedProject)
            },
        ) {
            Text("Save")
        }
    }
}