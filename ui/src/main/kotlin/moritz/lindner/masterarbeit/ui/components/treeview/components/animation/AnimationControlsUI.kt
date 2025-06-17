package moritz.lindner.masterarbeit.ui.components.treeview.components.animation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moritz.lindner.masterarbeit.epa.visitor.animation.EventLogAnimation
import moritz.lindner.masterarbeit.ui.components.treeview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.treeview.state.EpaViewModel

@Composable
fun AnimationControlsUI(
    playing: Boolean,
    stepSize: Long,
    multiplier: Float,
    sliderValue: Float,
    animation: EventLogAnimation<Long>?,
    viewModel: EpaViewModel,
    onClose: () -> Unit,
    onSliderChange: (Float) -> Unit,
    onPlayToggle: (Boolean) -> Unit,
    onForward: () -> Unit,
    onBackward: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Controls at the top
        ControlUI(
            isPlaying = playing,
            stepSize = stepSize,
            multiplier = multiplier,
            onButton = onPlayToggle,
            onForward = onForward,
            onBackward = onBackward,
            onClose = {
                viewModel.updateAnimation(AnimationState.Companion.Empty)
                onClose()
            },
        )

        // Slider and timestamps
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.Companion.fillMaxWidth(),
        ) {
            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    onSliderChange(newValue)

                    val timestamp = newValue.toLong()
                    val state = animation?.getActiveStatesAt(timestamp) ?: emptyList()

                    val animationState =
                        AnimationState(
                            time = timestamp,
                            currentTimeStates = state.toSet(),
                        )
                    viewModel.updateAnimation(animationState)
                },
                modifier = Modifier.Companion.fillMaxWidth(),
                valueRange =
                    (animation?.getFirst()?.first?.toFloat() ?: 0f)..(
                        animation?.getLast()?.first?.toFloat()
                            ?: 100f
                    ),
            )

            // Timeline labels
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val first = animation?.getFirst()?.first ?: 0
                val last = animation?.getLast()?.first ?: 0
                val current = viewModel.animationState.value.time

                Text("Start: $first", fontSize = 14.sp)
                Text("Now: $current", fontSize = 14.sp, fontWeight = FontWeight.Companion.Medium)
                Text("End: $last", fontSize = 14.sp)
            }
        }
    }
}
