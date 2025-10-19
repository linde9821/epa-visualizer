package moritz.lindner.masterarbeit.epa.features.layout.factory

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.TimeRadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.WalkerTreeLayout
import kotlin.math.PI

/** Factory for creating TreeLayout instances based on configuration. */
object LayoutFactory {
    /**
     * Creates a TreeLayout implementation based on the provided configuration.
     *
     * @param config The layout configuration specifying algorithm and
     *    parameters.
     * @return A TreeLayout instance configured according to the provided
     *    config.
     */
    fun create(config: LayoutConfig): TreeLayout = when (config) {
        is LayoutConfig.Walker -> WalkerTreeLayout(config.distance, config.yDistance)
        is LayoutConfig.RadialWalker -> RadialWalkerTreeLayout(
            config.layerSpace,
            config.margin.degreesToRadians(),
            config.rotation.degreesToRadians()
        )

        is LayoutConfig.DirectAngular -> DirectAngularPlacementTreeLayout(
            config.layerSpace,
            config.rotation.degreesToRadians()
        )

        is LayoutConfig.TimeRadialWalker -> TimeRadialWalkerTreeLayout(
            layerBaseUnit = config.layerBaseUnit,
            margin = config.margin.degreesToRadians(),
            rotation = config.rotation.degreesToRadians(),
            minCycleTimeDifference = config.minCycleTimeDifference,
            extendedPrefixAutomaton = config.extendedPrefixAutomaton,
        )
    }

    /** Converts degrees to radians. */
    private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f
}