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
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import kotlin.math.PI
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
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
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
            Divider(
                orientation = Orientation.Horizontal,
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = JewelTheme.contentColor.copy(alpha = 0.2f)
            )

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp, top = 5.dp)
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

    is LayoutConfig.TimeBasedRadialConfig -> when (paramName) {
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
        "umapK" -> config.umapK
        "Iterations" -> config.iterations
        "canvasWidth" -> config.canvasWidth
        "canvasHeight" -> config.canvasHeight
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
        "LABEL_OVERLAP_FORCE_STRENGTH" -> config.LABEL_OVERLAP_FORCE_STRENGTH
        "EDGE_LENGTH_FORCE_STRENGTH" -> config.EDGE_LENGTH_FORCE_STRENGTH
        "DISTRIBUTION_FORCE_STRENGTH" -> config.DISTRIBUTION_FORCE_STRENGTH
        else -> throw IllegalArgumentException("Unknown parameter $paramName")
    }

    is LayoutConfig.PartitionClusteringLayoutConfig -> when (paramName) {
        "enabled" -> config.enabled
        "umapK" -> config.umapK
        "umapIterations" -> config.umapIterations
        "canvasWidth" -> config.canvasWidth
        "canvasHeight" -> config.canvasHeight
        "lod" -> config.lod
        "useTotalStateCount" -> config.useTotalStateCount
        "useTotalEventCount" -> config.useTotalEventCount
        "useTotalTraceCount" -> config.useTotalTraceCount
        "useDeepestDepth" -> config.useDeepestDepth
        "useSplittingFactor" -> config.useSplittingFactor
        "useHasRepetition" -> config.useHasRepetition
        "useCombinedCycleTime" -> config.useCombinedCycleTime
        "useActivitySequenceEncoding" -> config.useActivitySequenceEncoding
        "useLempelZivComplexity" -> config.useLempelZivComplexity
        else -> throw IllegalArgumentException("Unknown parameter $paramName")
    }

    is LayoutConfig.PartitionSimilarityRadialLayoutConfig -> when (paramName) {
        "enabled" -> config.enabled
        "umapK" -> config.umapK
        "umapIterations" -> config.umapIterations
        "layerSpace" -> config.layerSpace
        "lod" -> config.lod
        "useTotalStateCount" -> config.useTotalStateCount
        "useTotalEventCount" -> config.useTotalEventCount
        "useTotalTraceCount" -> config.useTotalTraceCount
        "useDeepestDepth" -> config.useDeepestDepth
        "useSplittingFactor" -> config.useSplittingFactor
        "useHasRepetition" -> config.useHasRepetition
        "useCombinedCycleTime" -> config.useCombinedCycleTime
        "useActivitySequenceEncoding" -> config.useActivitySequenceEncoding
        "useLempelZivComplexity" -> config.useLempelZivComplexity
        else -> throw IllegalArgumentException("Unknown parameter $paramName")
    }
}

/** Converts degrees to radians. */
private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f