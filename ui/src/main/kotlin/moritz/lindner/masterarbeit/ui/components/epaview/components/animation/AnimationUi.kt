package moritz.lindner.masterarbeit.ui.components.epaview.components.animation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.animation.EventLogAnimation
import moritz.lindner.masterarbeit.epa.features.animation.SingleCaseAnimationBuilder
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitorWithProgressBar
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewModel
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography
import kotlin.math.roundToInt

@Composable
fun AnimationUi(
    filteredEpa: ExtendedPrefixAutomaton<Long>?,
    viewModel: EpaViewModel,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
) {
    var state: AnimationSelectionState by remember { mutableStateOf(AnimationSelectionState.NothingSelected) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(1.dp)
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(4.dp),
                ).padding(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Animation", style = JewelTheme.typography.h1TextStyle)
        }

        if (filteredEpa != null) {
            when (state) {
                AnimationSelectionState.NothingSelected -> {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DefaultButton(
                            onClick = {
                                state = AnimationSelectionState.SingleCase
                            },
                        ) {
                            Text("Select Case")
                        }

                        DefaultButton(
                            onClick = {
                                state = AnimationSelectionState.WholeLog
                            },
                        ) {
                            Text("Animate whole event log (${filteredEpa.eventLogName})")
                        }
                    }
                }

                is AnimationSelectionState.SingleCase -> {
                    SingleCaseAnimationUI(filteredEpa, backgroundDispatcher, viewModel) {
                        state = AnimationSelectionState.NothingSelected
                    }
                }

                AnimationSelectionState.WholeLog ->
                    TimelineSliderWholeLogUi(filteredEpa, viewModel, backgroundDispatcher) {
                        state = AnimationSelectionState.NothingSelected
                    }
            }
        } else {
            CircularProgressIndicator()
        }
    }
}

fun findStepSize(
    start: Long,
    end: Long,
    maxSteps: Int = 10_000,
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
fun TimelineSliderSingleCaseUi(
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    dispatcher: CoroutineDispatcher,
    viewModel: EpaViewModel,
    case: String,
) {
    var isLoading by remember { mutableStateOf(true) }
    var animation by remember { mutableStateOf<EventLogAnimation<Long>?>(null) }
    val singleCaseAnimationBuilder = SingleCaseAnimationBuilder<Long>(case)
    var sliderValue by remember { mutableStateOf(0f) }

    LaunchedEffect(extendedPrefixAutomaton) {
        isLoading = true
        withContext(dispatcher) {
            extendedPrefixAutomaton
                .copy()
                .acceptDepthFirst(AutomatonVisitorWithProgressBar(singleCaseAnimationBuilder, "casesAnimation"))
            yield()
            animation = singleCaseAnimationBuilder.build()

            viewModel.updateAnimation(
                AnimationState.Empty,
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
                        AnimationState.Empty
                    } else {
                        AnimationState(
                            time = state.first,
                            currentTimeStates = setOf(state.second),
                        )
                    }
                viewModel.updateAnimation(animationState)
            },
            modifier = Modifier.fillMaxWidth(),
            valueRange = 0f..(animation!!.totalAmountOfEvents.toFloat() - 1f),
            steps = animation!!.totalAmountOfEvents - 1,
        )
    }
}
