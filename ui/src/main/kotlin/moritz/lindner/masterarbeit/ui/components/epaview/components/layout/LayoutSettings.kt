package moritz.lindner.masterarbeit.ui.components.epaview.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text

@Composable
fun LayoutSettings(
    radius: Float,
    onRadiusChange: (Float) -> Unit,
    margin: Float,
    onMarginChange: (Float) -> Unit,
    layouts: List<LayoutSelection>,
    selectedLayout: LayoutSelection,
    onLayoutSelectionChange: (LayoutSelection) -> Unit,
) {
    val selectedIndex = layouts.indexOf(selectedLayout)

    Column {
        Text("Layout Algorithm:")
        ListComboBox(
            items = layouts.map { it.name },
            selectedIndex = selectedIndex,
            onSelectedItemChange = { index ->
                onLayoutSelectionChange(layouts[index])
            },
            modifier = Modifier.width(200.dp)
        )

        // TODO: make lower layout options depend on layout
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
    }
}
