package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.asCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.epaview.components.project.ProjectUi
import moritz.lindner.masterarbeit.ui.components.newproject.NewProjectUi
import moritz.lindner.masterarbeit.ui.components.projectselection.ProjectSelectionUi
import moritz.lindner.masterarbeit.ui.state.ApplicationState
import moritz.lindner.masterarbeit.ui.state.ApplicationState.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

@Composable
fun EPAVisualizerUi() {
    var state: ApplicationState by remember { mutableStateOf(Start()) }

    Column {
        when (val currentState = state) {
            is Start -> ProjectSelectionUi(
                error = currentState.error,
                onProjectOpen = {
                    state = ProjectSelected(it)
                },
                onNewProject = {
                    state = NewProject
                },
                onError = { error ->
                    state = currentState.copy(error = error)
                }
            )

            is NewProject -> NewProjectUi(
                onAbort = { state = Start() },
                onProjectCreated = { state = ProjectSelected(it) }
            )

            is ProjectSelected -> {
                val executor = remember { buildDispatcher() }
                ProjectUi(
                    project = currentState.project,
                    executor.asCoroutineDispatcher(),
                    onClose = {
                        executor.shutdownNow()
                        state = Start()
                    },
                )
            }
        }
    }
}

fun buildDispatcher(): ExecutorService {
    val threadCount = maxOf(2, Runtime.getRuntime().availableProcessors() - 1)
    var threadCounter = 0
    val threadFactory = ThreadFactory { runnable ->
        Thread(runnable, "EPA-Worker-${++threadCounter}").apply {
            isDaemon = true
        }
    }
    return Executors.newFixedThreadPool(threadCount, threadFactory)
}