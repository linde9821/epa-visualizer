package moritz.lindner.masterarbeit.ui.components.epaview.components.statistics

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun StatisticItem(
    label: String,
    value: Any,
) {
    Row(
        modifier =
            Modifier
                .padding(vertical = 2.dp),
    ) {
        Text("$label: ", style = JewelTheme.typography.regular,)
        Text(value.toString(), style = JewelTheme.typography.regular,)
    }
}
