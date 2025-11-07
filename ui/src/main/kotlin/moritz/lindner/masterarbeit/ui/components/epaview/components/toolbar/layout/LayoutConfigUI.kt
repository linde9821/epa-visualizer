package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.factory.ParameterInfo
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import kotlin.math.roundToInt

@Composable
fun LayoutConfigUI(
    config: LayoutConfig,
    onConfigChange: (LayoutConfig) -> Unit,
) {
    var currentConfig by remember(config) { mutableStateOf(config) }

    LazyColumn {
        items(currentConfig.getParameters().toList()) { (paramName, info) ->
            val currentValue = getCurrentConfigValue(currentConfig, paramName)

            when (info) {
                is ParameterInfo.BooleanParameterInfo -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${info.name}:")
                        Checkbox(
                            checked = currentValue as Boolean,
                            onCheckedChange = { value ->
                                currentConfig = currentConfig.updateParameter(paramName, value)
                            }
                        )
                    }
                }

                is ParameterInfo.NumberParameterInfo<*> -> {
                    when (info.steps) {
                        is Int -> {
                            Text("${info.name}: $currentValue (steps ${info.steps})")
                            Slider(
                                value = (currentValue as Int).toFloat(),
                                onValueChange = { value ->
                                    currentConfig = currentConfig.updateParameter(
                                        paramName,
                                        value.roundToInt()
                                    )
                                },
                                valueRange = (info.min as Int).toFloat()..(info.max as Int).toFloat(),
                                steps = info.steps as Int,
                            )
                        }

                        is Float -> {
                            Text("${info.name}: ${"%.1f".format(currentValue)}")
                            Slider(
                                value = currentValue as Float,
                                onValueChange = { value ->
                                    currentConfig = currentConfig.updateParameter(paramName, value)
                                },
                                valueRange = (info.min as Float)..(info.max as Float),
                                steps = (((info.max as Float) - (info.min as Float)) / (info.steps as Float)).toInt()
                            )
                        }
                    }
                }

                is ParameterInfo.EnumParameterInfo<*> -> {
                    var selectedIndex by remember { mutableStateOf(0) }
                    Text(info.name)
                    ListComboBox(
                        items = info.selectionOptions.map { it.name },
                        selectedIndex = selectedIndex,
                        onSelectedItemChange = { index ->
                            selectedIndex = index
                            currentConfig = currentConfig.updateParameter(paramName, info.selectionOptions[index])
                        },
                    )
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
            ) {
                DefaultButton(
                    onClick = {
                        onConfigChange(currentConfig)
                    },
                ) {
                    Text("Update Layout")
                }
            }
        }
    }
}

private fun getCurrentConfigValue(
    config: LayoutConfig,
    paramName: String
): Any = when (config) {
    is LayoutConfig.WalkerConfig -> when (paramName) {
        "distance" -> config.distance
        "layerSpace" -> config.layerSpace
        "enabled" -> config.enabled
        "lod" -> config.lod
        else -> throw IllegalArgumentException("Unknown parameter $paramName")
    }

    is LayoutConfig.DirectAngularConfig -> when (paramName) {
        "layerSpace" -> config.layerSpace
        "rotation" -> config.rotation
        "enabled" -> config.enabled
        "lod" -> config.lod
        else -> throw IllegalArgumentException("Unknown parameter $paramName")
    }

    is LayoutConfig.RadialWalkerConfig -> when (paramName) {
        "layerSpace" -> config.layerSpace
        "margin" -> config.margin
        "rotation" -> config.rotation
        "enabled" -> config.enabled
        "lod" -> config.lod
        else -> throw IllegalArgumentException("Unknown parameter $paramName")
    }

    is LayoutConfig.RadialWalkerTimeConfig -> when (paramName) {
        "layerBaseUnit" -> config.layerBaseUnit
        "margin" -> config.margin
        "rotation" -> config.rotation
        "enabled" -> config.enabled
        "minCycleTimeDifference" -> config.minCycleTimeDifference
        "lod" -> config.lod
        else -> throw IllegalArgumentException("Unknown parameter $paramName")
    }

    is LayoutConfig.StateClusteringLayoutConfig -> when (paramName) {
        "useGraphEmbedding" -> config.useGraphEmbedding
        "graphEmbeddingDims" -> config.graphEmbeddingDims
        "walkLength" -> config.walkLength
        "walksPerVertex" -> config.walksPerVertex
        "windowSize" -> config.windowSize
        "useFeatureEmbedding" -> config.useFeatureEmbedding
        "featureEmbeddingDims" -> config.featureEmbeddingDims
        "useDepthFeature" -> config.useDepthFeature
        "useOutgoingTransitions" -> config.useOutgoingTransitions
        "usePartitionValue" -> config.usePartitionValue
        "useSequenceLength" -> config.useSequenceLength
        "useCycleTime" -> config.useCycleTime
        "usePathLength" -> config.usePathLength
        "useActivity" -> config.useActivity
        "reductionMethod" -> config.reductionMethod
        "umapK" -> config.umapK
        "Iterations" -> config.iterations
        "canvasWidth" -> config.canvasWidth
        "canvasHeight" -> config.canvasHeight
        "nodeRadius" -> config.nodeRadius
        "padding" -> config.padding
        "useForceDirected" -> config.useForceDirected
        "repulsionStrength" -> config.repulsionStrength
        "forceDirectedLayoutIterations" -> config.forceDirectedLayoutIterations
        "useResolveOverlap" -> config.useResolveOverlap
        "enabled" -> config.enabled
        "lod" -> config.lod
        else -> throw IllegalArgumentException("Unknown parameter $paramName")
    }

    is LayoutConfig.PRTLayoutConfig -> when (paramName) {
        "enabled" -> config.enabled
        "initialization" -> config.initializer
        "iterations" -> config.iterations
        "lod" -> config.lod
        "minEdgeLength" -> config.minEdgeLength
        "maxEdgeLength" -> config.maxEdgeLength
        else -> throw IllegalArgumentException("Unknown parameter $paramName")
    }

    is LayoutConfig.PartitionClusteringLayoutConfig -> when (paramName) {
        "enabled" -> config.enabled
        "umapK" -> config.umapK
        "umapIterations" -> config.umapIterations
        "canvasWidth" -> config.canvasWidth
        "canvasHeight" -> config.canvasHeight
        "nodeRadius" -> config.nodeRadius
        "padding" -> config.padding
        "lod" -> config.lod
        else -> throw IllegalArgumentException("Unknown parameter $paramName")
    }

    is LayoutConfig.PartitionSimilarityRadialLayoutConfig -> when (paramName) {
        "enabled" -> config.enabled
        "umapK" -> config.umapK
        "umapIterations" -> config.umapIterations
        "layerSpace" -> config.layerSpace
        "lod" -> config.lod
        else -> throw IllegalArgumentException("Unknown parameter $paramName")
    }
}