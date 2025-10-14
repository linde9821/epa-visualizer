package moritz.lindner.masterarbeit.ui.components.epaview.components.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.ui.components.epaview.state.TaskProgressState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.HorizontalProgressBar
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun EpaProgress(
    currentProgress: TaskProgressState,
    modifier: Modifier = Modifier.Companion
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = currentProgress.taskName,
            style = JewelTheme.typography.h2TextStyle,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(
            text = "${
                "%.1f".format(
                    currentProgress.percentage * 100f,
                )
            }% (${currentProgress.current} / ${currentProgress.total})",
            style = JewelTheme.typography.regular,
            color = JewelTheme.contentColor.copy(alpha = 0.8f),
        )
        Spacer(modifier = Modifier.padding(12.dp))
        HorizontalProgressBar(
            progress = currentProgress.percentage,
            modifier = Modifier.width(450.dp),
        )
    }
}