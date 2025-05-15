package moritz.lindner.masterarbeit.ui.components

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
import kotlinx.coroutines.asCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.ApplicationState
import java.util.concurrent.Executors

@Composable
fun EPAVisualizerUi() {
    val backgroundDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    var state: ApplicationState by remember { mutableStateOf(ApplicationState.NoFileSelected) }
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(10.dp),
        ) {
            when (val currentState = state) {
                ApplicationState.NoFileSelected ->
                    FileSelectionUi { file ->
                        state = ApplicationState.FileSelected(file)
                    }

                is ApplicationState.FileSelected ->
                    EpaConstructionUi(
                        file = currentState.file,
                        onAbort = { state = ApplicationState.NoFileSelected },
                        onStartConstructionStart = { builder ->
                            state = ApplicationState.EpaConstructionRunning(builder)
                        },
                    )

                is ApplicationState.EpaConstructionRunning ->
                    ConstructEpaUi(scope, backgroundDispatcher, currentState.builder) { epa, tree ->
                        state = ApplicationState.EpaConstructed(epa, tree)
                    }

                is ApplicationState.EpaConstructed ->
                    EpaViewUi(
                        currentState.extendedPrefixAutomata,
                        currentState.tree,
                        backgroundDispatcher,
                        onClose = {
                            state = ApplicationState.NoFileSelected
                        },
                    )
            }
        }
    }
}
