package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.layout

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.factory.ParameterInfo
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text

@Composable
fun LayoutConfigUI(
    config: LayoutConfig,
    onConfigChange: (LayoutConfig) -> Unit
) {
    LazyColumn {
        items(config.getParameters().toList()) { (paramName, info) ->
            val currentValue = when (config) {
                is LayoutConfig.WalkerConfig -> when (paramName) {
                    "distance" -> config.distance
                    "layerSpace" -> config.layerSpace
                    "enabled" -> config.enabled
                    else -> throw IllegalArgumentException("Unknown parameter $paramName")
                }

                is LayoutConfig.DirectAngularConfig -> when (paramName) {
                    "layerSpace" -> config.layerSpace
                    "rotation" -> config.rotation
                    "enabled" -> config.enabled
                    else -> throw IllegalArgumentException("Unknown parameter $paramName")
                }

                is LayoutConfig.RadialWalkerConfig -> when (paramName) {
                    "layerSpace" -> config.layerSpace
                    "margin" -> config.margin
                    "rotation" -> config.rotation
                    "enabled" -> config.enabled
                    else -> throw IllegalArgumentException("Unknown parameter $paramName")
                }

                is LayoutConfig.TimeRadialWalkerConfig -> when (paramName) {
                    "layerBaseUnit" -> config.layerBaseUnit
                    "margin" -> config.margin
                    "rotation" -> config.rotation
                    "enabled" -> config.enabled
                    "minCycleTimeDifference" -> config.minCycleTimeDifference
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
                    "umapIterations" -> config.umapIterations
                    "canvasWidth" -> config.canvasWidth
                    "canvasHeight" -> config.canvasHeight
                    "nodeRadius" -> config.nodeRadius
                    "padding" -> config.padding
                    "useForceDirected" -> config.useForceDirected
                    "repulsionStrength" -> config.repulsionStrength
                    "forceDirectedLayoutIterations" -> config.forceDirectedLayoutIterations
                    "useResolveOverlap" -> config.useResolveOverlap
                    "enabled" -> config.enabled
                    else -> throw IllegalArgumentException("Unknown parameter $paramName")
                }

                is LayoutConfig.PRTLayoutConfig -> when (paramName) {
                    "enabled" -> config.enabled
                    "initialization" -> config.initializer
                    "iterations" -> config.iterations
                    else -> throw IllegalArgumentException("Unknown parameter $paramName")
                }

                is LayoutConfig.PartitionClusteringLayoutConfig -> when(paramName) {
                    "enabled" -> config.enabled
                    "umapK" -> config.umapK
                    "umapIterations" -> config.umapIterations
                    "canvasWidth" -> config.canvasWidth
                    "canvasHeight" -> config.canvasHeight
                    "nodeRadius" -> config.nodeRadius
                    "padding" -> config.padding
                    else -> throw IllegalArgumentException("Unknown parameter $paramName")
                }

                is LayoutConfig.PartitionSimilarityRadialLayoutConfig -> when(paramName) {
                    "enabled" -> config.enabled
                    "umapK" -> config.umapK
                    "umapIterations" -> config.umapIterations
                    "layerSpace" -> config.layerSpace
                    else -> throw IllegalArgumentException("Unknown parameter $paramName")
                }
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

                is ParameterInfo.NumberParameterInfo<*> -> {
                    when (info.step) {
                        is Int -> {
                            Text("${info.name}: $currentValue")
                            Slider(
                                value = (currentValue as Int).toFloat(),
                                onValueChange = { value ->
                                    onConfigChange(
                                        config.updateParameter(
                                            paramName,
                                            value.toInt()
                                        )
                                    )
                                },
                                valueRange = (info.min as Int).toFloat()..(info.max as Int).toFloat(),
                                steps = info.step as Int
                            )
                        }

                        is Float -> {
                            Text("${info.name}: ${"%.1f".format(currentValue)}")
                            Slider(
                                value = currentValue as Float,
                                onValueChange = { value -> onConfigChange(config.updateParameter(paramName, value)) },
                                valueRange = (info.min as Float)..(info.max as Float),
                                steps = (((info.max as Float) - (info.min as Float)) / (info.step as Float)).toInt()
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
                            onConfigChange(config.updateParameter(paramName, info.selectionOptions[index]))
                        },
                    )

                }
            }
        }
    }
}