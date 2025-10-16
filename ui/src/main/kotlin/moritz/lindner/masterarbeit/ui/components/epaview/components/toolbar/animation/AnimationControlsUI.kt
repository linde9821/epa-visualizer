package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.animation


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.features.animation.EventLogAnimation
import moritz.lindner.masterarbeit.ui.common.Formatting.asFormattedLocalDateTime
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun AnimationControlsUI(
    isPlaying: Boolean,
    stepSize: Long,
    multiplier: Float,
    sliderValue: Float,
    animation: EventLogAnimation<Long>?,
    epaStateManager: EpaStateManager,
    onClose: () -> Unit,
    onSliderChange: (Float) -> Unit,
    onPlayToggle: (Boolean) -> Unit,
    onForward: () -> Unit,
    onBackward: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(onClick = {
                epaStateManager.updateAnimation(AnimationState.Empty)
                onClose()
            }) {
                Icon(key = AllIconsKeys.Actions.Close, contentDescription = "Close", tint = JewelTheme.contentColor)
            }
        }

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
                epaStateManager.updateAnimation(animationState)
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
            val current = epaStateManager.animationState.value.time

            Text("Start: ${first.asFormattedLocalDateTime()}")
            Text("Current: ${current.asFormattedLocalDateTime()}")
            Text("End: ${last.asFormattedLocalDateTime()}")
        }

        ControlUI(
            isPlaying = isPlaying,
            stepSize = stepSize,
            multiplier = multiplier,
            onButton = onPlayToggle,
            onForward = onForward,
            onBackward = onBackward,
        )
    }
}
