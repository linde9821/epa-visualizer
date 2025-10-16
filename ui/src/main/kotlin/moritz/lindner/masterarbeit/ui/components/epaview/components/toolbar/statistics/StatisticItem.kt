package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography
import java.text.DecimalFormat

@Composable
fun StatisticItem(
    label: String,
    value: Number,
) {
    val formatter = DecimalFormat("#,###")
    StatisticItem(
        label = label,
        value = formatter.format(value)
    )
}

@Composable
fun StatisticItem(
    label: String,
    value: String,
) {
    SelectionContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val formattedLabel = label.take(19)
            Text("$formattedLabel: ", style = JewelTheme.typography.regular)
            Text(value, style = JewelTheme.typography.regular)
        }
    }
}
