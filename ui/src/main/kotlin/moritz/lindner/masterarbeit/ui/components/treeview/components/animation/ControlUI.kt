package moritz.lindner.masterarbeit.ui.components.treeview.components.animation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.sp

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
            Modifier.Companion
                .fillMaxWidth()
                .padding(4.dp),
        verticalAlignment = Alignment.Companion.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left: Control Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onBackward) {
                Icon(Icons.Default.FastRewind, contentDescription = "Rewind")
            }

            IconButton(onClick = { onButton(!isPlaying) }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play",
                )
            }

            IconButton(onClick = onForward) {
                Icon(Icons.Default.FastForward, contentDescription = "Forward")
            }
        }

        Text(
            text = "$stepSize × $multiplier = ${(stepSize * multiplier).toLong()}",
            fontSize = 16.sp,
            modifier = Modifier.Companion.padding(horizontal = 16.dp),
        )

        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}
