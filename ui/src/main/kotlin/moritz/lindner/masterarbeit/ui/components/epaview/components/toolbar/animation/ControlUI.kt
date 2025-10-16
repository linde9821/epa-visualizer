package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.animation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.ui.common.Formatting.asFormattedLocalDateTime
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun ControlUI(
    isPlaying: Boolean = false,
    stepSize: Long,
    multiplier: Float,
    onButton: (Boolean) -> Unit,
    onForward: () -> Unit,
    onBackward: () -> Unit,
) {
    Column (
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackward) {
                Icon(
                    key = AllIconsKeys.Actions.Play_back,
                    tint = JewelTheme.contentColor,
                    contentDescription = "Rewind"
                )
            }

            IconButton(onClick = { onButton(!isPlaying) }) {
                Icon(
                    key = if (isPlaying) AllIconsKeys.Actions.Pause else AllIconsKeys.Actions.Play_forward,
                    contentDescription = if (isPlaying) "Stop" else "Play",
                    tint = JewelTheme.contentColor
                )
            }

            IconButton(onClick = onForward) {
                Icon(
                    key = AllIconsKeys.Actions.Play_forward,
                    contentDescription = "Forward",
                    tint = JewelTheme.contentColor
                )
            }
        }

        Text(
            text = "$stepSize x $multiplier = ${(stepSize * multiplier).toLong()}",
        )
    }
}
