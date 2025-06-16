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
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitorProgressBar
import moritz.lindner.masterarbeit.epa.visitor.animation.EventLogAnimation
import moritz.lindner.masterarbeit.epa.visitor.animation.SingleCaseAnimationVisitor
import moritz.lindner.masterarbeit.epa.visitor.animation.WholeEventLogAnimationVisitor
import moritz.lindner.masterarbeit.epa.visitor.case.CaseEventCollectorVisitor
import moritz.lindner.masterarbeit.ui.components.treeview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.treeview.state.EpaViewModel
import moritz.lindner.masterarbeit.ui.logger

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
                AnimationSelectionState.WholeLog ->
                    WholeCaseAnimationUi(epa, viewModel, backgroundDispatcher) {
                        state = AnimationSelectionState.NothingSelected
                    }
            }
        }
    }
}

fun findStepSize(
    start: Long,
    end: Long,
    maxSteps: Int = 500,
): Long {
    val totalRange = end - start
    if (totalRange <= 0) return 1L

    // Ideal raw step
    val rawStep = totalRange / maxSteps

    // Define preferred "round" step sizes
    val preferredSteps =
        listOf(
            // Millisecond-level
            1L,
            2L,
            5L,
            10L,
            20L,
            50L,
            100L,
            200L,
            250L,
            500L,
            // Second-level
            1_000L, // 1 sec
            2_000L,
            5_000L,
            10_000L,
            15_000L,
            30_000L, // up to 30 sec
            // Minute-level
            60_000L, // 1 min
            120_000L, // 2 min
            300_000L, // 5 min
            600_000L, // 10 min
            900_000L, // 15 min
            1_800_000L, // 30 min
            // Hour-level
            3_600_000L, // 1 hour
            7_200_000L, // 2 hours
            10_800_000L, // 3 hours
            21_600_000L, // 6 hours
            43_200_000L, // 12 hours
            // Day-level
            86_400_000L, // 1 day
            172_800_000L, // 2 days
            604_800_000L, // 7 days (1 week)
        )

    // Pick the smallest preferred step >= rawStep
    return preferredSteps.firstOrNull { it >= rawStep } ?: rawStep
}

@Composable
fun WholeCaseAnimationUi(
    epa: ExtendedPrefixAutomata<Long>,
    viewModel: EpaViewModel,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    Column(modifier = Modifier.Companion.padding(16.dp)) {
        Row {
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

        TimelineSliderWholeLogUi(epa, backgroundDispatcher, viewModel)
    }
}

@Composable
fun TimelineSliderWholeLogUi(
    extendedPrefixAutomata: ExtendedPrefixAutomata<Long>,
    dispatcher: CoroutineDispatcher,
    viewModel: EpaViewModel,
) {
    var isLoading by remember { mutableStateOf(true) }
    var animation by remember { mutableStateOf<EventLogAnimation<Long>?>(null) }
    val eventLogAnimationVisitor = WholeEventLogAnimationVisitor<Long>(extendedPrefixAutomata.eventLogName)
    var sliderValue by remember { mutableStateOf(0f) }

    var playing by remember { mutableStateOf(false) }

    LaunchedEffect(extendedPrefixAutomata) {
        isLoading = true
        withContext(dispatcher) {
            extendedPrefixAutomata
                .copy()
                .acceptDepthFirst(AutomataVisitorProgressBar(eventLogAnimationVisitor, "casesAnimation"))
            yield()
            animation = eventLogAnimationVisitor.build()

            sliderValue = animation!!.getFirst().first.toFloat()

            viewModel.updateAnimation(
                AnimationState(
                    current = emptyList(),
                ),
            )
        }
        isLoading = false
    }

    LaunchedEffect(playing) {
        if (playing && animation != null) {
            val first = animation!!.getFirst().first
            val last = animation!!.getLast().first

            val stepSize =
                findStepSize(
                    start = first,
                    end = last,
                )

            for (timestamp in sliderValue.toLong()..last step stepSize) {
                logger.info { "running animation $timestamp" }
                sliderValue = timestamp.toFloat()
                val state = animation!!.getActiveStatesAt(timestamp)
                viewModel.updateAnimation(AnimationState(current = state))
                delay(100) // playback speed
            }
            playing = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else if (animation != null) {
        Column {
            Row {
                Button(
                    onClick = {
                        playing = true
                    },
                ) {
                    Text("Play")
                }

                Button(
                    onClick = {
                        playing = false
                    },
                ) {
                    Text("Pause")
                }

                Text("Cursor at ${sliderValue.toLong()}")
            }

            Row {
                Slider(
                    value = sliderValue,
                    onValueChange = { newValue ->
                        sliderValue = newValue

                        val timestamp = sliderValue.toLong()
                        logger.info { "Getting states at $timestamp" }
                        val state = animation!!.getActiveStatesAt(timestamp)
                        logger.info { "Active states count: ${state.size}" }

                        val animationState =
                            AnimationState(
                                current = state,
                            )
                        viewModel.updateAnimation(animationState)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    valueRange = animation!!.getFirst().first.toFloat()..animation!!.getLast().first.toFloat(),
                )
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
    var caseEventCollectorVisitor by remember { mutableStateOf(CaseEventCollectorVisitor<Long>()) }

    LaunchedEffect(epa) {
        isLoading = true
        withContext(backgroundDispatcher) {
            selectedCase = null
            caseEventCollectorVisitor = CaseEventCollectorVisitor()
            epa.copy()?.acceptDepthFirst(AutomataVisitorProgressBar(caseEventCollectorVisitor, "case animation"))
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
                                items(caseEventCollectorVisitor.cases.toList()) { case ->
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
fun TimelineSliderSingleCaseUi(
    extendedPrefixAutomata: ExtendedPrefixAutomata<Long>,
    dispatcher: CoroutineDispatcher,
    viewModel: EpaViewModel,
    case: String,
) {
    var isLoading by remember { mutableStateOf(true) }
    var animation by remember { mutableStateOf<EventLogAnimation<Long>?>(null) }
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
//        Slider(
//            value = sliderValue,
//            onValueChange = { newValue ->
//                sliderValue = newValue
//
//                val index = sliderValue.roundToInt().coerceIn(0, animation!!.totalAmountOfEvents - 1)
//                val state = animation!!.getNthEntry(index)
//
//                logger.info { "Getting state at $index" }
//
//                val animationState =
//                    if (state == null) {
//                        AnimationState(
//                            current = emptyList(),
//                        )
//                    } else {
//                        AnimationState(
//                            current = listOf(state.second),
//                        )
//                    }
//                viewModel.updateAnimation(animationState)
//            },
//            modifier = Modifier.Companion.fillMaxWidth(),
//            valueRange = 0f..(animation!!.totalAmountOfEvents.toFloat() - 1f),
//            steps = animation!!.totalAmountOfEvents,
//        )
    }
}
