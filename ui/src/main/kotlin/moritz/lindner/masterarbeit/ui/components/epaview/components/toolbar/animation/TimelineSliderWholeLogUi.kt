package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.AnimationService
import moritz.lindner.masterarbeit.epa.features.animation.EventLogAnimation
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.jewel.ui.component.CircularProgressIndicatorBig

@Composable
fun TimelineSliderWholeLogUi(
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    epaStateManager: EpaStateManager,
    dispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    val animationService = AnimationService<Long>()

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
        epaStateManager.updateAnimation(AnimationState.Empty)
        withContext(dispatcher) {
            eventLogAnimation = animationService.createFullLogAnimation(
                extendedPrefixAutomaton,
                10L,
                Long::plus,
            )
        }
        epaStateManager.updateAnimation(AnimationState.Empty)
        sliderValue = eventLogAnimation!!.getFirst().first.toFloat()
        isLoading = false
    }

    LaunchedEffect(isPlaying, extendedPrefixAutomaton) {
        epaStateManager.updateAnimation(AnimationState.Empty)
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
                epaStateManager.updateAnimation(
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
        CircularProgressIndicatorBig()
    } else if (eventLogAnimation != null) {
        AnimationControlsUI(
            isPlaying = isPlaying,
            stepSize = stepSize,
            multiplier = multiplier,
            sliderValue = sliderValue,
            animation = eventLogAnimation,
            epaStateManager = epaStateManager,
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
