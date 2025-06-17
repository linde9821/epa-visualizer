package moritz.lindner.masterarbeit.ui.components.treeview.components.animation

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
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitorProgressBar
import moritz.lindner.masterarbeit.epa.visitor.animation.EventLogAnimation
import moritz.lindner.masterarbeit.epa.visitor.animation.WholeEventLogAnimationVisitor
import moritz.lindner.masterarbeit.ui.components.treeview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.treeview.state.EpaViewModel
import moritz.lindner.masterarbeit.ui.logger

@Composable
fun TimelineSliderWholeLogUi(
    extendedPrefixAutomata: ExtendedPrefixAutomata<Long>,
    viewModel: EpaViewModel,
    dispatcher: CoroutineDispatcher,
    onClose: () -> Unit,
) {
    var isLoading by remember(extendedPrefixAutomata) { mutableStateOf(true) }
    var animation by remember(extendedPrefixAutomata) { mutableStateOf<EventLogAnimation<Long>?>(null) }
    var sliderValue by remember(extendedPrefixAutomata) { mutableStateOf(0f) }
    var playing by remember(extendedPrefixAutomata) { mutableStateOf(false) }
    val speed = 17L
    var stepSize by remember(extendedPrefixAutomata) { mutableStateOf(1L) }
    var multiplier by remember(extendedPrefixAutomata) { mutableStateOf(1.0f) }

    LaunchedEffect(extendedPrefixAutomata) {
        isLoading = true
        playing = false
        viewModel.updateAnimation(AnimationState.Companion.Empty)
        withContext(dispatcher) {
            val eventLogAnimationVisitor = WholeEventLogAnimationVisitor<Long>(extendedPrefixAutomata.eventLogName)
            extendedPrefixAutomata
                .copy()
                .acceptDepthFirst(AutomataVisitorProgressBar(eventLogAnimationVisitor, "casesAnimation"))
            yield()
            animation =
                eventLogAnimationVisitor.build(
                    epsilon = 10L,
                    increment = Long::plus,
                )
        }
        viewModel.updateAnimation(AnimationState.Companion.Empty)
        sliderValue = animation!!.getFirst().first.toFloat()
        isLoading = false
    }

    LaunchedEffect(playing, extendedPrefixAutomata) {
        viewModel.updateAnimation(AnimationState.Companion.Empty)
        if (playing && animation != null) {
            val first = animation!!.getFirst().first
            val last = animation!!.getLast().first

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

                val state = animation!!.getActiveStatesAt(timestamp)
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
            playing = false
            playing = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else if (animation != null) {
        AnimationControlsUI(
            playing = playing,
            stepSize = stepSize,
            multiplier = multiplier,
            sliderValue = sliderValue,
            animation = animation,
            viewModel = viewModel,
            onClose = onClose,
            onSliderChange = {
                sliderValue = it
            },
            onPlayToggle = {
                playing = it
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
