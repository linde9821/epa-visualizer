package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state.plots

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
        Text(
            text = value,
            style = JewelTheme.Companion.typography.h3TextStyle,
            fontWeight = FontWeight.Companion.Bold
        )
        Text(
            text = label,
            style = JewelTheme.Companion.typography.small
        )
    }
}