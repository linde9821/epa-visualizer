package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.construction.EpaConstructionUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.EpaTreeViewUi
import moritz.lindner.masterarbeit.ui.components.fileselection.FileSelectionUi
import moritz.lindner.masterarbeit.ui.components.loadingepa.ConstructEpaUi
import moritz.lindner.masterarbeit.ui.logger
import moritz.lindner.masterarbeit.ui.state.ApplicationState

@Composable
fun EPAVisualizerUi(backgroundDispatcher: ExecutorCoroutineDispatcher) {
    var state: ApplicationState by remember { mutableStateOf(ApplicationState.NoFileSelected) }
    val scope = rememberCoroutineScope()

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("Unknown error") }

//    val lightBlueColorPalette =
//        lightColors(
//            primary = Color(0xFF1976D2),
//            primaryVariant = Color(0xFF1565C0),
//            secondary = Color(0xFF90CAF9),
//            background = Color(0xFFF5F8FB),
//            surface = Color(0xFFFFFFFF),
//            onPrimary = Color.White,
//            onSecondary = Color.Black,
//            onBackground = Color.Black,
//            onSurface = Color.Black,
//        )

//    MaterialTheme(colors = lightBlueColorPalette) {
//        Surface(
//            elevation = 8.dp,
//            color = MaterialTheme.colors.surface,
//            modifier = Modifier.fillMaxSize(),
//        ) {
    Column {
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
                        state = ApplicationState.EpaConstructionRunning(currentState.file, builder)
                    },
                )

            is ApplicationState.EpaConstructionRunning -> {
                ConstructEpaUi(scope, backgroundDispatcher, currentState.builder, { epa ->
                    state = ApplicationState.EpaConstructed(epa)
                }, {
                    state = ApplicationState.FileSelected(currentState.selectedFile)
                }) { error, e ->
                    showErrorDialog = true
                    errorMessage = error
                    logger.error(e) { error }
                }

                if (showErrorDialog) {
                    AlertDialog(
                        onDismissRequest = { showErrorDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showErrorDialog = false
                                state = ApplicationState.FileSelected(currentState.selectedFile)
                            }) {
                                Text("Ok")
                            }
                        },
                        title = {
                            Text("Error")
                        },
                        text = {
                            Text(errorMessage)
                        },
                    )
                }
            }

            is ApplicationState.EpaConstructed ->
                EpaTreeViewUi(
                    currentState.extendedPrefixAutomaton,
                    backgroundDispatcher,
                    onClose = {
                        state = ApplicationState.NoFileSelected
                    },
                )
        }
    }
}
