package moritz.lindner.masterarbeit.epa.features.layout.factory

import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.WalkerTreeLayout
import kotlin.math.PI

object LayoutFactory {
    fun create(config: LayoutConfig): TreeLayout = when (config) {
        is LayoutConfig.Walker -> WalkerTreeLayout(config.distance, config.yDistance)
        is LayoutConfig.RadialWalker -> RadialWalkerTreeLayout(config.layerSpace, config.margin.degreesToRadians())
        is LayoutConfig.DirectAngular -> DirectAngularPlacementTreeLayout(config.layerSpace)
    }

    private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f
}