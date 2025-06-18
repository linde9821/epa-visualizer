package moritz.lindner.masterarbeit.ui.components.treeview.components.statistics

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        Text("$label:", style = MaterialTheme.typography.body2)
        Text(value.toString(), style = MaterialTheme.typography.body2)
    }
}
