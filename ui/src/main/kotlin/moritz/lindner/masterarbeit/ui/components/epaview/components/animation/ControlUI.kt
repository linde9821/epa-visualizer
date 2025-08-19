package moritz.lindner.masterarbeit.ui.components.epaview.components.animation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ControlUI(
    isPlaying: Boolean = false,
    stepSize: Long,
    multiplier: Float,
    onButton: (Boolean) -> Unit,
    onForward: () -> Unit,
    onBackward: () -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onBackward) {
                Icon(Icons.Default.FastRewind, tint = JewelTheme.contentColor, contentDescription = "Rewind")
            }

            IconButton(onClick = { onButton(!isPlaying) }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play",
                    tint = JewelTheme.contentColor
                )
            }

            IconButton(onClick = onForward) {
                Icon(Icons.Default.FastForward, contentDescription = "Forward", tint = JewelTheme.contentColor)
            }
        }

        Text(
            text = "$stepSize × $multiplier = ${(stepSize * multiplier).toLong()}",
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = JewelTheme.contentColor)
        }
    }
}
