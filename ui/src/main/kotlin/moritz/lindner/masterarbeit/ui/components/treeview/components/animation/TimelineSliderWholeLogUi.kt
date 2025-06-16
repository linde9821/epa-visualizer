package moritz.lindner.masterarbeit.ui.components.treeview.components.animation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
    dispatcher: CoroutineDispatcher,
    viewModel: EpaViewModel,
) {
    var isLoading by remember(extendedPrefixAutomata) { mutableStateOf(true) }
    var animation by remember(extendedPrefixAutomata) { mutableStateOf<EventLogAnimation<Long>?>(null) }
    var sliderValue by remember(extendedPrefixAutomata) { mutableStateOf(0f) }
    var playing by remember(extendedPrefixAutomata) { mutableStateOf(false) }

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
            animation = eventLogAnimationVisitor.build()
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
            val playbackSpeed = 100L

            val stepSize =
                findStepSize(
                    start = first,
                    end = last,
                )

            for (timestamp in sliderValue.toLong()..last step stepSize) {
                yield()
                logger.info { "running animation $timestamp" }
                sliderValue = timestamp.toFloat()
                val state = animation!!.getActiveStatesAt(timestamp)
                yield()
                viewModel.updateAnimation(
                    AnimationState(
                        time = timestamp,
                        current = state.toSet(),
                    ),
                )
                yield()
                delay(playbackSpeed)
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
                        val state = animation!!.getActiveStatesAt(timestamp)

                        val animationState =
                            AnimationState(
                                time = timestamp,
                                current = state.toSet(),
                            )
                        viewModel.updateAnimation(animationState)
                    },
                    modifier = Modifier.Companion.fillMaxWidth(),
                    valueRange = animation!!.getFirst().first.toFloat()..animation!!.getLast().first.toFloat(),
                )
            }
        }
    }
}
