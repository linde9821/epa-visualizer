package moritz.lindner.masterarbeit.ui.components.epaview.components.animation

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.animation.EventLogAnimation
import moritz.lindner.masterarbeit.epa.features.animation.WholeEventLogAnimationBuilder
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitorWithProgressBar
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewModel
import moritz.lindner.masterarbeit.ui.logger

@Composable
fun TimelineSliderWholeLogUi(
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    viewModel: EpaViewModel,
    dispatcher: CoroutineDispatcher,
    onClose: () -> Unit,
) {
    var isLoading by remember(extendedPrefixAutomaton) { mutableStateOf(true) }
    var eventLogAnimation by remember(extendedPrefixAutomaton) { mutableStateOf<EventLogAnimation<Long>?>(null) }
    var sliderValue by remember(extendedPrefixAutomaton) { mutableStateOf(0f) }
    var isPlaying by remember(extendedPrefixAutomaton) { mutableStateOf(false) }
    val speed = 17L // 60fps
    var stepSize by remember(extendedPrefixAutomaton) { mutableStateOf(1L) }
    var multiplier by remember(extendedPrefixAutomaton) { mutableStateOf(1.0f) }

    LaunchedEffect(extendedPrefixAutomaton) {
        isLoading = true
        isPlaying = false
        viewModel.updateAnimation(AnimationState.Companion.Empty)
        withContext(dispatcher) {
            val eventLogAnimationVisitor = WholeEventLogAnimationBuilder<Long>(extendedPrefixAutomaton.eventLogName)
            extendedPrefixAutomaton
                .copy()
                .acceptDepthFirst(AutomatonVisitorWithProgressBar(eventLogAnimationVisitor, "casesAnimation"))
            yield()
            eventLogAnimation =
                eventLogAnimationVisitor.build(
                    epsilon = 10L,
                    increment = Long::plus,
                )
        }
        viewModel.updateAnimation(AnimationState.Empty)
        sliderValue = eventLogAnimation!!.getFirst().first.toFloat()
        isLoading = false
    }

    LaunchedEffect(isPlaying, extendedPrefixAutomaton) {
        viewModel.updateAnimation(AnimationState.Empty)
        if (isPlaying && eventLogAnimation != null) {
            val first = eventLogAnimation!!.getFirst().first
            val last = eventLogAnimation!!.getLast().first

            val playbackSpeed = speed
            stepSize =
                findStepSize(
                    start = first,
                    end = last,
                )

            var timestamp = sliderValue.toLong()

            while (timestamp <= last) {
                val dynamicStepSize = (stepSize * multiplier).toLong()

                logger.info { "running animation $timestamp" }
                sliderValue = timestamp.toFloat()

                val state = eventLogAnimation!!.getActiveStatesAt(timestamp)
                viewModel.updateAnimation(
                    AnimationState(
                        time = timestamp,
                        currentTimeStates = state.toSet(),
                    ),
                )

                yield()
                delay(playbackSpeed)
                timestamp += dynamicStepSize
            }
            isPlaying = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else if (eventLogAnimation != null) {
        AnimationControlsUI(
            isPlaying = isPlaying,
            stepSize = stepSize,
            multiplier = multiplier,
            sliderValue = sliderValue,
            animation = eventLogAnimation,
            viewModel = viewModel,
            onClose = onClose,
            onSliderChange = {
                sliderValue = it
            },
            onPlayToggle = {
                isPlaying = it
            },
            onForward = {
                multiplier += 0.25f
            },
            onBackward = {
                multiplier -= 0.25f
            },
        )
    }
}
