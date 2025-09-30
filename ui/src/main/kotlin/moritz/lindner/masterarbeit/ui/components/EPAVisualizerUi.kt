package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.epaview.components.ProjectUi
import moritz.lindner.masterarbeit.ui.components.newproject.NewProjectUi
import moritz.lindner.masterarbeit.ui.components.projectselection.ProjectSelectionUi
import moritz.lindner.masterarbeit.ui.state.ApplicationState

@Composable
fun EPAVisualizerUi(backgroundDispatcher: ExecutorCoroutineDispatcher) {
    var state: ApplicationState by remember { mutableStateOf(ApplicationState.Start()) }

    Column {
        when (val currentState = state) {
            is ApplicationState.Start -> ProjectSelectionUi(
                error = currentState.error,
                onProjectOpen = {
                    state = ApplicationState.ProjectSelected(it)
                },
                onNewProject = {
                    state = ApplicationState.NewProject
                },
                onError = { error ->
                    state = currentState.copy(error = error)
                }
            )

            is ApplicationState.NewProject -> NewProjectUi(
                onAbort = { state = ApplicationState.Start() },
                onProjectCreated = { state = ApplicationState.ProjectSelected(it) }
            )

            is ApplicationState.ProjectSelected -> {
                ProjectUi(
                    project = currentState.project,
                    backgroundDispatcher,
                    onClose = {
                        state = ApplicationState.Start()
                    },
                )
            }
        }
    }
}

