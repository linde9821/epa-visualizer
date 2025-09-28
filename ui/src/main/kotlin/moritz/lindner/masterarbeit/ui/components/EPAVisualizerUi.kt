package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.epaview.components.EpaTreeViewUi
import moritz.lindner.masterarbeit.ui.components.fileselection.ProjectSelectionUi
import moritz.lindner.masterarbeit.ui.components.loadingepa.ConstructEpaUi
import moritz.lindner.masterarbeit.ui.components.project.NewProjectUi
import moritz.lindner.masterarbeit.ui.logger
import moritz.lindner.masterarbeit.ui.state.ApplicationState

@Composable
fun EPAVisualizerUi(backgroundDispatcher: ExecutorCoroutineDispatcher) {
    var state: ApplicationState by remember { mutableStateOf(ApplicationState.Start()) }
    val scope = rememberCoroutineScope()

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

