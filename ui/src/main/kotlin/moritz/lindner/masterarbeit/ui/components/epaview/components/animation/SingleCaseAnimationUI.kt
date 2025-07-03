package moritz.lindner.masterarbeit.ui.components.epaview.components.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.withContext
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.features.animation.EventsByCasesCollector
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitorWithProgressBar
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewModel

@Composable
fun SingleCaseAnimationUI(
    epa: ExtendedPrefixAutomata<Long>,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    viewModel: EpaViewModel,
    onClose: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedCase by remember { mutableStateOf<String?>(null) }
    var eventsByCasesCollector by remember { mutableStateOf(EventsByCasesCollector<Long>()) }

    LaunchedEffect(epa) {
        isLoading = true
        withContext(backgroundDispatcher) {
            selectedCase = null
            eventsByCasesCollector = EventsByCasesCollector()
            epa.copy().acceptDepthFirst(AutomatonVisitorWithProgressBar(eventsByCasesCollector, "case animation"))
        }
        isLoading = false
    }

    Column(modifier = Modifier.Companion.padding(16.dp)) {
        Row {
            Button(
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(48.dp),
                onClick = {
                    showDialog = true
                    selectedCase = null
                    viewModel.updateAnimation(
                        AnimationState.Companion.Empty,
                    )
                },
            ) {
                Text("Select Case")
            }
            Spacer(Modifier.Companion.width(8.dp))

            if (selectedCase != null) {
                Text("Case: $selectedCase")
            }

            Button(
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(48.dp),
                onClick = {
                    viewModel.updateAnimation(
                        AnimationState.Companion.Empty,
                    )
                    onClose()
                },
            ) {
                Text("Close")
            }
        }

        Spacer(Modifier.Companion.height(8.dp))

        if (selectedCase != null) {
            TimelineSliderSingleCaseUi(epa, backgroundDispatcher, viewModel, selectedCase!!)
        }

        if (showDialog) {
            DialogWindow(
                onCloseRequest = { showDialog = false },
                title = "Single Case Animation",
                visible = showDialog,
            ) {
                if (!isLoading) {
                    Box(
                        Modifier.Companion.padding(16.dp),
                    ) {
                        Row {
                            LazyColumn {
                                items(eventsByCasesCollector.cases.toList()) { case ->
                                    val isSelected = case == selectedCase
                                    Text(
                                        text = case,
                                        modifier =
                                            Modifier.Companion
                                                .padding(8.dp)
                                                .background(if (isSelected) Color.Companion.LightGray else Color.Companion.Transparent)
                                                .clickable { selectedCase = case }
                                                .padding(8.dp),
                                    )
                                }
                            }
                            Column {
                                Button(
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    modifier = Modifier.height(48.dp),
                                    onClick = {
                                        showDialog = false
                                    },
                                ) {
                                    Text("Run")
                                }
                                Button(
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    modifier = Modifier.height(48.dp),
                                    onClick = { showDialog = false },
                                ) {
                                    Text("Close")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
