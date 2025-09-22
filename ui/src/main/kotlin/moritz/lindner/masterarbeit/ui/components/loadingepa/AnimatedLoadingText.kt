package moritz.lindner.masterarbeit.ui.components.loadingepa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun AnimatedLoadingText(baseText: String) {
    var dotCount by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500) // Half second delay between changes
            dotCount = if (dotCount >= 3) 1 else dotCount + 1
        }
    }

    Text(
        text = baseText + ".".repeat(dotCount),
        style = JewelTheme.defaultTextStyle,
    )
}