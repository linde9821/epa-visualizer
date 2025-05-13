import ApplicationState.EpaConstructed
import ApplicationState.EpaConstructionRunning
import ApplicationState.FileSelected
import ApplicationState.NoFileSelected
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import epa.ConstructEpa
import epa.EpaConstruction
import epa.EpaView
import epa.FileSelection
import kotlinx.coroutines.asCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.treelayout.tree.EPATreeNode
import java.io.File
import java.util.concurrent.Executors

sealed class ApplicationState {
    data object NoFileSelected : ApplicationState()

    data class FileSelected(
        val file: File,
    ) : ApplicationState()

    data class EpaConstructionRunning(
        val builder: ExtendedPrefixAutomataBuilder<Long>,
    ) : ApplicationState()

    data class EpaConstructed(
        val extendedPrefixAutomata: ExtendedPrefixAutomata<Long>,
        val tree: EPATreeNode<Long>,
    ) : ApplicationState()
}

@Composable
fun EPAVisualizer() {
    val backgroundDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    var state: ApplicationState by remember { mutableStateOf(NoFileSelected) }
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(10.dp),
        ) {
            when (val currentState = state) {
                NoFileSelected -> {
                    FileSelection { file ->
                        state = FileSelected(file)
                    }
                }

                is FileSelected -> {
                    EpaConstruction(
                        file = currentState.file,
                        onAbort = { state = NoFileSelected },
                        onStartConstructionStart = { builder ->
                            state = EpaConstructionRunning(builder)
                        },
                    )
                }

                is EpaConstructionRunning -> {
                    ConstructEpa(scope, backgroundDispatcher, currentState.builder) { epa, tree ->
                        state = EpaConstructed(epa, tree)
                    }
                }

                is EpaConstructed ->
                    EpaView(
                        currentState.extendedPrefixAutomata,
                        currentState.tree,
                        backgroundDispatcher,
                    ) {
                        state = NoFileSelected
                    }
            }
        }
    }
}

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            state =
                WindowState(
                    placement = WindowPlacement.Maximized,
                    isMinimized = false,
                ),
            title = "EPA Visualizer",
        ) {
            EPAVisualizer()
        }
    }
