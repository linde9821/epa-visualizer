package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.layout

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    LazyColumn {
        items(config.getParameters().toList()) { (paramName, info) ->
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

                is LayoutConfig.TimeRadialWalker -> when (paramName) {
                    "layerBaseUnit" -> config.multiplayer
                    "margin" -> config.margin
                    "rotation" -> config.rotation
                    "enabled" -> config.render
                    "minCycleTimeDifference" -> config.minCycleTimeDifference
                    else -> throw IllegalArgumentException("Unknown parameter $paramName")
                }

                is LayoutConfig.ClusteringLayoutConfig -> when (paramName) {
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
                    "enabled" -> config.render
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
                            Text("${info.name} (Int): $currentValue")
                            Slider(
                                value = (currentValue as Int).toFloat(),
                                onValueChange = { value -> onConfigChange(config.updateParameter(paramName, value.toInt())) },
                                valueRange = (info.min as Int).toFloat()..(info.max as Int).toFloat(),
                                steps = info.step as Int
                            )
                        }
                        is Float -> {
                            Text("${info.name} (Float): ${"%.1f".format(currentValue)}")
                            Slider(
                                value = currentValue as Float,
                                onValueChange = { value -> onConfigChange(config.updateParameter(paramName, value)) },
                                valueRange = (info.min as Float)..(info.max as Float),
                                steps = (((info.max as Float) - (info.min as Float)) / (info.step as Float)).toInt()
                            )
                        }
                    }
                }
            }
        }
    }
}