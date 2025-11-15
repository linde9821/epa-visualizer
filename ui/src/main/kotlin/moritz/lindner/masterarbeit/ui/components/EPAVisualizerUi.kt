package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.ExecutorService
import moritz.lindner.masterarbeit.ui.components.epaview.components.project.ProjectUi
import moritz.lindner.masterarbeit.ui.components.newproject.NewProjectUi
import moritz.lindner.masterarbeit.ui.components.projectselection.ProjectSelectionUi
import moritz.lindner.masterarbeit.ui.state.ApplicationState
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

@Composable
fun EPAVisualizerUi() {
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
                val (backgroundDispatcher, executor) = remember { buildDispatcher() }
                ProjectUi(
                    project = currentState.project,
                    backgroundDispatcher,
                    onClose = {
                        executor.shutdownNow()
                        state = ApplicationState.Start()
                    },
                )
            }
        }
    }
}

fun buildDispatcher(): Pair<ExecutorCoroutineDispatcher, ExecutorService> {
    val threadCount = maxOf(2, Runtime.getRuntime().availableProcessors() - 1)
    var threadCounter = 0
    val threadFactory = ThreadFactory { runnable ->
        Thread(runnable, "EPA-Worker-${++threadCounter}").apply {
            isDaemon = true
        }
    }
    val executor = Executors.newFixedThreadPool(threadCount, threadFactory)
    return executor.asCoroutineDispatcher() to executor
}