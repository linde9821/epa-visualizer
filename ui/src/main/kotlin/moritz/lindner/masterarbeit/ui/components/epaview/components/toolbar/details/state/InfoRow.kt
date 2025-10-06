package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = JewelTheme.Companion.typography.regular
        )
        Text(
            text = value,
            style = JewelTheme.Companion.typography.medium,
            fontWeight = FontWeight.Companion.Medium
        )
    }
}