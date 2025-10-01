package moritz.lindner.masterarbeit.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
private fun InfoText(text: String) {
    Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis, color = JewelTheme.globalColors.text.info)
}
