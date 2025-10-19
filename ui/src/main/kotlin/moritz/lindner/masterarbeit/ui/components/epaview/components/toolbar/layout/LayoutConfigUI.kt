package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.layout

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.factory.ParameterInfo
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text

@Composable
fun LayoutConfigUI(
    config: LayoutConfig,
    onConfigChange: (LayoutConfig) -> Unit
) {
    config.getParameters().forEach { (paramName, info) ->
        val currentValue = when (config) {
            is LayoutConfig.Walker -> when (paramName) {
                "distance" -> config.distance
                "yDistance" -> config.yDistance
                "enabled" -> config.render
                else -> throw IllegalArgumentException("Unknown parameter $paramName")
            }

            is LayoutConfig.DirectAngular -> when (paramName) {
                "layerSpace" -> config.layerSpace
                "rotation" -> config.rotation
                "enabled" -> config.render
                else -> throw IllegalArgumentException("Unknown parameter $paramName")
            }

            is LayoutConfig.RadialWalker -> when (paramName) {
                "layerSpace" -> config.layerSpace
                "margin" -> config.margin
                "rotation" -> config.rotation
                "enabled" -> config.render
                else -> throw IllegalArgumentException("Unknown parameter $paramName")
            }

            is LayoutConfig.TimeRadialWalker ->  when (paramName) {
                "layerBaseUnit" -> config.multiplayer
                "margin" -> config.margin
                "rotation" -> config.rotation
                "enabled" -> config.render
                "minCycleTimeDifference" -> config.minCycleTimeDifference
                else -> throw IllegalArgumentException("Unknown parameter $paramName")
            }

            is LayoutConfig.Semantic -> config.render
        }

        when (info) {
            is ParameterInfo.BooleanParameterInfo -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${info.name}:")
                    Checkbox(
                        checked = currentValue as Boolean,
                        onCheckedChange = { value -> onConfigChange(config.updateParameter(paramName, value)) }
                    )
                }
            }

            is ParameterInfo.FloatParameterInfo -> {
                Text("${info.name}: ${"%.1f".format(currentValue)}")

                Slider(
                    value = currentValue as Float,
                    onValueChange = { value -> onConfigChange(config.updateParameter(paramName, value)) },
                    valueRange = info.min..info.max,
                    steps = ((info.max - info.min) / info.step).toInt()
                )
            }
        }
    }
}