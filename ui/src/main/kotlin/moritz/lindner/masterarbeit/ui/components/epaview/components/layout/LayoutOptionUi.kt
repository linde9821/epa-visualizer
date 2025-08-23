package moritz.lindner.masterarbeit.ui.components.epaview.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun LayoutUi(
    modifier: Modifier = Modifier,
    onUpdate: (LayoutConfig) -> Unit,
) {
    val layouts = listOf(
        LayoutConfig.RadialWalker(),
        LayoutConfig.Walker(),
        LayoutConfig.DirectAngular(),
    )
    var layoutSelectionIndex by remember { mutableStateOf(0) }
    var selectedLayout by remember(layoutSelectionIndex) { mutableStateOf(layouts[layoutSelectionIndex]) }

    Column(
        modifier = modifier.padding(start = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Layout Settings", style = JewelTheme.typography.h1TextStyle)

        Divider(
            orientation = Orientation.Horizontal,
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = JewelTheme.contentColor.copy(alpha = 0.2f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Algorithm:")
            ListComboBox(
                items = layouts.map { it.name },
                selectedIndex = layoutSelectionIndex,
                onSelectedItemChange = { index ->
                    layoutSelectionIndex = index
                    onUpdate(selectedLayout)
                },
            )
        }


        LayoutConfigUI(selectedLayout) { newConfig ->
            selectedLayout = newConfig
            onUpdate(selectedLayout)
        }
    }
}

@Composable
fun LayoutConfigUI(
    config: LayoutConfig,
    onConfigChange: (LayoutConfig) -> Unit
) {
    Column {
        config.getParameters().forEach { (paramName, info) ->
            val currentValue = when (config) {
                is LayoutConfig.Walker -> when (paramName) {
                    "distance" -> config.distance
                    "yDistance" -> config.yDistance
                    else -> 0f
                }

                is LayoutConfig.DirectAngular -> when (paramName) {
                    "layerSpace" -> config.layerSpace
                    else -> 0f
                }

                is LayoutConfig.RadialWalker -> when (paramName) {
                    "layerSpace" -> config.layerSpace
                    "margin" -> config.margin
                    else -> 0f
                }
            }

            Text("${info.name}: ${"%.1f".format(currentValue)}")
            Slider(
                value = currentValue,
                onValueChange = { value -> onConfigChange(config.updateParameter(paramName, value)) },
                valueRange = info.min..info.max,
                steps = ((info.max - info.min) / info.step).toInt()
            )
        }
    }
}