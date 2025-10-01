package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.withContext
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text

@Composable
fun SingleCaseAnimationUI(
    epa: ExtendedPrefixAutomaton<Long>,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    epaStateManager: EpaStateManager,
    onClose: () -> Unit,
) {
    val epaService = EpaService<Long>()

    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedCase by remember { mutableStateOf<String?>(null) }
    var eventsByCase by remember { mutableStateOf(emptyMap<String, List<Event<Long>>>()) }

    LaunchedEffect(epa) {
        isLoading = true
        withContext(backgroundDispatcher) {
            selectedCase = null
            eventsByCase = epaService.getEventsByCase(epa)
        }
        isLoading = false
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultButton(
                onClick = {
                    showDialog = true
                    selectedCase = null
                    epaStateManager.updateAnimation(AnimationState.Empty)
                }
            ) {
                Text("Select Case")
            }

            if (selectedCase != null) {
                Text(
                    text = "Case: $selectedCase",
                    style = JewelTheme.defaultTextStyle,
                    modifier = Modifier.weight(1f) // Takes available space
                )
            }

            DefaultButton(
                onClick = {
                    epaStateManager.updateAnimation(AnimationState.Empty)
                    onClose()
                }
            ) {
                Text("Close")
            }
        }
        Spacer(Modifier.height(8.dp))

        if (selectedCase != null) {
            TimelineSliderSingleCaseUi(epa, backgroundDispatcher, epaStateManager, selectedCase!!)
        }

        if (showDialog) {
            DialogWindow(
                onCloseRequest = { showDialog = false },
                title = "Single Case Animation",
                visible = showDialog,
            ) {
                if (!isLoading) {
                    Box(
                        Modifier.padding(16.dp),
                    ) {
                        Row {
                            LazyColumn {
                                items(eventsByCase.keys.toList()) { case ->
                                    val isSelected = case == selectedCase
                                    Text(
                                        text = case,
                                        modifier =
                                            Modifier
                                                .padding(8.dp)
                                                .background(if (isSelected) Color.LightGray else Color.Transparent)
                                                .clickable {
                                                    selectedCase = case
                                                    showDialog = false
                                                }
                                                .padding(8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
