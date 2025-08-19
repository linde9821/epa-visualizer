package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
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
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.ErrorDefaultBanner
import org.jetbrains.jewel.ui.theme.defaultBannerStyle

@Composable
fun EPAVisualizerUi(backgroundDispatcher: ExecutorCoroutineDispatcher) {
    var state: ApplicationState by remember { mutableStateOf(ApplicationState.NoFileSelected) }
    val scope = rememberCoroutineScope()

    Column {
        when (val currentState = state) {
            ApplicationState.NoFileSelected ->
                FileSelectionUi { file ->
                    state = ApplicationState.FileSelected(file, null)
                }

            is ApplicationState.FileSelected ->
                EpaConstructionUi(
                    state = currentState,
                    onAbort = { state = ApplicationState.NoFileSelected },
                    onStartConstructionStart = { builder ->
                        state = ApplicationState.EpaConstructionRunning(currentState.file, builder)
                    },
                )

            is ApplicationState.EpaConstructionRunning -> {
                ConstructEpaUi(scope, backgroundDispatcher, currentState.builder, { epa ->
                    state = ApplicationState.EpaConstructed(epa)
                }, {
                    state = ApplicationState.FileSelected(currentState.selectedFile, null)
                }) { error, e ->
                    logger.error(e) { error }
                    state = ApplicationState.FileSelected(currentState.selectedFile, error)
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
