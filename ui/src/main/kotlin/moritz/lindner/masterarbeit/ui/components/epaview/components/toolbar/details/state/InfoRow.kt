package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.typography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoRow(label: String, value: String, hintText: String? = null) {
    if (hintText != null) {
        Tooltip(
            modifier = Modifier.padding(end = 8.dp),
            tooltip = {
                Text(
                    text = hintText,
                    style = JewelTheme.typography.regular
                )
            }
        ) {
            SelectionContainer {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = label,
                        style = JewelTheme.typography.regular
                    )
                    Text(
                        text = value,
                        style = JewelTheme.typography.medium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    } else {
        SelectionContainer {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = JewelTheme.typography.regular
                )
                Text(
                    text = value,
                    style = JewelTheme.typography.medium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}