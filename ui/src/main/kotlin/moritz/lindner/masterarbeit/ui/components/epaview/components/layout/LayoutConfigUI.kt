package moritz.lindner.masterarbeit.ui.components.epaview.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text

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
                    "rotation" -> config.rotation
                    else -> 0f
                }

                is LayoutConfig.RadialWalker -> when (paramName) {
                    "layerSpace" -> config.layerSpace
                    "margin" -> config.margin
                    "rotation" -> config.rotation
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