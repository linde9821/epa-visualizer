package moritz.lindner.masterarbeit.ui.components.treeview.components.animation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import moritz.lindner.masterarbeit.epa.visitor.animation.EventLogAnimation
import moritz.lindner.masterarbeit.ui.components.treeview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.treeview.state.EpaViewModel

@Composable
fun AnimationControlsUI(
    isPlaying: Boolean,
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
        modifier = Modifier.fillMaxSize(),
    ) {
        ControlUI(
            isPlaying = isPlaying,
            stepSize = stepSize,
            multiplier = multiplier,
            onButton = onPlayToggle,
            onForward = onForward,
            onBackward = onBackward,
            onClose = {
                viewModel.updateAnimation(AnimationState.Empty)
                onClose()
            },
        )

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
            modifier = Modifier.fillMaxWidth(),
            valueRange =
                (animation?.getFirst()?.first?.toFloat() ?: 0f)..(
                    animation?.getLast()?.first?.toFloat()
                        ?: 100f
                ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val first = animation?.getFirst()?.first ?: 0
            val last = animation?.getLast()?.first ?: 0
            val current = viewModel.animationState.value.time

            Text("Start: $first", fontSize = 14.sp)
            Text("Now: $current", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("End: $last", fontSize = 14.sp)
        }
    }
}
