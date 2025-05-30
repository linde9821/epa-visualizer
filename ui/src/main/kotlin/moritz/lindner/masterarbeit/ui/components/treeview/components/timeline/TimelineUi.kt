package moritz.lindner.masterarbeit.ui.components.treeview.components.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Slider
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitorProgressBar
import moritz.lindner.masterarbeit.epa.visitor.case.CaseAnimation
import moritz.lindner.masterarbeit.epa.visitor.case.CaseAnimationVisitor
import moritz.lindner.masterarbeit.epa.visitor.case.CaseVisitor
import moritz.lindner.masterarbeit.ui.components.treeview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.treeview.state.EpaViewModel
import moritz.lindner.masterarbeit.ui.logger
import kotlin.math.floor

@Composable
fun TimelineUi(
    epa: ExtendedPrefixAutomata<Long>?,
    viewModel: EpaViewModel,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
) {
    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCase by remember { mutableStateOf<String?>(null) }
    var caseVisitor by remember { mutableStateOf(CaseVisitor<Long>()) }

    LaunchedEffect(epa) {
        isLoading = true
        withContext(backgroundDispatcher) {
            selectedCase = null
            caseVisitor = CaseVisitor()
            epa?.copy()?.acceptDepthFirst(AutomataVisitorProgressBar(caseVisitor, "case"))
        }
        isLoading = false
    }

    Column(modifier = Modifier.Companion.padding(16.dp)) {
        Row {
            Button(onClick = { showDialog = true }) {
                Text("Select Case")
            }
            Spacer(Modifier.Companion.width(8.dp))

            if (selectedCase != null) {
                Text("Case: $selectedCase")
            }
        }

        Spacer(Modifier.Companion.height(8.dp))

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
                                items(caseVisitor.cases.toList()) { case ->
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
                                Button(onClick = {
                                    showDialog = false
                                }) {
                                    Text("Run")
                                }
                                Button(onClick = { showDialog = false }) {
                                    Text("Close")
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedCase != null && epa != null) {
            TimeSlider(epa, backgroundDispatcher, viewModel, selectedCase!!)
        }
    }
}

@Composable
fun TimeSlider(
    extendedPrefixAutomata: ExtendedPrefixAutomata<Long>,
    dispatcher: CoroutineDispatcher,
    viewModel: EpaViewModel,
    case: String,
) {
    var isLoading by remember { mutableStateOf(true) }
    var animation by remember { mutableStateOf<CaseAnimation<Long>?>(null) }
    val caseAnimationVisitor = CaseAnimationVisitor<Long>(case)
    var sliderValue by remember { mutableStateOf(0f) }

    LaunchedEffect(extendedPrefixAutomata) {
        isLoading = true
        withContext(dispatcher) {
            extendedPrefixAutomata
                .copy()
                .acceptDepthFirst(AutomataVisitorProgressBar(caseAnimationVisitor, "casesAnimation"))
            yield()
            animation = caseAnimationVisitor.build()
        }
        isLoading = false
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else if (animation != null) {
        Slider(
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                val x = floor(sliderValue).toInt() - 1
                logger.info { "total ${animation!!.totalAmountOfEvents}" }
                logger.info { "state $sliderValue" }
                val state = animation!!.getNthEntry(x)
                viewModel.updateAnimation(
                    if (state == null) AnimationState(emptyList()) else AnimationState(listOf(state)),
                )
                logger.info { "Event: $state for $x" }
            },
            modifier = Modifier.Companion.fillMaxWidth(),
            valueRange = 0f..animation!!.totalAmountOfEvents.toFloat(),
            steps = animation!!.totalAmountOfEvents - 1,
        )
    }
}
