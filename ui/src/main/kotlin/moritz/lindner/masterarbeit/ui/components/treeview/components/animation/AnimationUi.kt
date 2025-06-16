package moritz.lindner.masterarbeit.ui.components.treeview.components.animation

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
import moritz.lindner.masterarbeit.epa.visitor.case.CaseVisitor
import moritz.lindner.masterarbeit.epa.visitor.case.SingleCaseAnimationVisitor
import moritz.lindner.masterarbeit.ui.components.treeview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.treeview.state.EpaViewModel
import moritz.lindner.masterarbeit.ui.logger
import kotlin.math.roundToInt

sealed class AnimationSelectionState {
    data object NothingSelected : AnimationSelectionState()

    data object WholeLog : AnimationSelectionState()

    data object SingleCase : AnimationSelectionState()
}

@Composable
fun AnimationUi(
    epa: ExtendedPrefixAutomata<Long>?,
    viewModel: EpaViewModel,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
) {
    var state: AnimationSelectionState by remember { mutableStateOf(AnimationSelectionState.NothingSelected) }

    if (epa != null) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            when (val current = state) {
                AnimationSelectionState.NothingSelected -> {
                    Button(
                        onClick = {
                            state = AnimationSelectionState.SingleCase
                        },
                    ) {
                        Text("Select Case")
                    }

                    Button(
                        onClick = {
                            state = AnimationSelectionState.WholeLog
                        },
                    ) {
                        Text("Animate whole event log (${epa.eventLogName})")
                    }
                }
                is AnimationSelectionState.SingleCase -> {
                    SingleCaseAnimationUI(epa, backgroundDispatcher, viewModel) {
                        state = AnimationSelectionState.NothingSelected
                    }
                }
                AnimationSelectionState.WholeLog -> TODO()
            }
        }
    }
}

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
            Button(onClick = {
                showDialog = true
                selectedCase = null
                viewModel.updateAnimation(
                    AnimationState.Empty,
                )
            }) {
                Text("Select Case")
            }
            Spacer(Modifier.Companion.width(8.dp))
            if (selectedCase != null) {
                Text("Case: $selectedCase")
            }

            Button(onClick = {
                viewModel.updateAnimation(
                    AnimationState.Empty,
                )
                onClose()
            }) {
                Text("Close")
            }
        }

        Spacer(Modifier.Companion.height(8.dp))

        if (selectedCase != null) {
            TimelineSliderUi(epa, backgroundDispatcher, viewModel, selectedCase!!)
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
        }
    }
}

@Composable
fun TimelineSliderUi(
    extendedPrefixAutomata: ExtendedPrefixAutomata<Long>,
    dispatcher: CoroutineDispatcher,
    viewModel: EpaViewModel,
    case: String,
) {
    var isLoading by remember { mutableStateOf(true) }
    var animation by remember { mutableStateOf<CaseAnimation<Long>?>(null) }
    val singleCaseAnimationVisitor = SingleCaseAnimationVisitor<Long>(case)
    var sliderValue by remember { mutableStateOf(0f) }

    LaunchedEffect(extendedPrefixAutomata) {
        isLoading = true
        withContext(dispatcher) {
            extendedPrefixAutomata
                .copy()
                .acceptDepthFirst(AutomataVisitorProgressBar(singleCaseAnimationVisitor, "casesAnimation"))
            yield()
            animation = singleCaseAnimationVisitor.build()

            val (_, state) = animation!!.getFirst()
            viewModel.updateAnimation(
                AnimationState(
                    current = listOf(state),
                ),
            )
        }
        isLoading = false
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else if (animation != null) {
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = newValue

                val index = sliderValue.roundToInt().coerceIn(0, animation!!.totalAmountOfEvents - 1)
                val state = animation!!.getNthEntry(index)

                logger.info { "Getting state at $index" }

                val animationState =
                    if (state == null) {
                        AnimationState(
                            current = emptyList(),
                        )
                    } else {
                        AnimationState(
                            current = listOf(state.second),
                        )
                    }
                viewModel.updateAnimation(animationState)
            },
            modifier = Modifier.Companion.fillMaxWidth(),
            valueRange = 0f..(animation!!.totalAmountOfEvents.toFloat() - 1f),
            steps = animation!!.totalAmountOfEvents,
        )
    }
}
