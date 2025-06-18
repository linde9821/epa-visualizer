package moritz.lindner.masterarbeit.ui.components.treeview.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import moritz.lindner.masterarbeit.ui.components.RadioButtonSingleSelectionColumn
import moritz.lindner.masterarbeit.ui.components.treeview.layout.LayoutSelection

@Composable
fun LayoutSettings(
    radius: Float,
    onRadiusChange: (Float) -> Unit,
    margin: Float,
    onMarginChange: (Float) -> Unit,
    layouts: List<LayoutSelection>,
    onLayoutSelectionChange: (LayoutSelection) -> Unit,
) {
    Column {
        Text("radius (width): ${"%.1f".format(radius)}")

        Slider(
            value = radius,
            onValueChange = { onRadiusChange(it) },
            valueRange = 10.0f..1000.0f,
        )

        Text("margin (width): ${"%.1f".format(margin)}")

        Slider(
            value = margin,
            onValueChange = { onMarginChange(it) },
            valueRange = 0.0f..360.0f,
        )

        Text("Layout Algorithm", style = MaterialTheme.typography.subtitle1)
        RadioButtonSingleSelectionColumn(layouts.map { option -> Pair(option, option.name) }) { layout, _ ->
            onLayoutSelectionChange(layout)
        }
    }
}
