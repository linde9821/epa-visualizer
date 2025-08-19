package moritz.lindner.masterarbeit.ui.components.epaview.components.animation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moritz.lindner.masterarbeit.epa.features.animation.EventLogAnimation
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewModel
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

            Text("Start: ${first.asFormattedLocalDateTime()}")
            Text("Now: ${current.asFormattedLocalDateTime()}")
            Text("End: ${last.asFormattedLocalDateTime()}")
        }
    }
}

fun Long.asFormattedLocalDateTime(): String {
    val localDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(this),
        ZoneId.systemDefault()
    )
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
    val formattedDate = localDateTime.format(formatter)

    return formattedDate
}
