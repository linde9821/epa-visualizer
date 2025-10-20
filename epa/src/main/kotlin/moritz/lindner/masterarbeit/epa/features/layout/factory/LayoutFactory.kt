package moritz.lindner.masterarbeit.epa.features.layout.factory

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.TimeRadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.semanticlayout.ClusteringLayout
import moritz.lindner.masterarbeit.epa.features.layout.tree.EPATreeNode
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
    fun createTreeLayout(config: LayoutConfig, root: EPATreeNode): TreeLayout = when (config) {
        is LayoutConfig.Walker -> {
            WalkerTreeLayout(
                distance = config.distance,
                yDistance = config.yDistance,
                tree = root
            )
        }

        is LayoutConfig.RadialWalker -> {
            RadialWalkerTreeLayout(
                tree = root,
                layerSpace = config.layerSpace,
                margin = config.margin.degreesToRadians(),
                rotation = config.rotation.degreesToRadians(),
            )
        }

        is LayoutConfig.DirectAngular -> {
            DirectAngularPlacementTreeLayout(
                tree = root,
                layerSpace = config.layerSpace,
                rotation = config.rotation.degreesToRadians()
            )
        }

        is LayoutConfig.TimeRadialWalker -> {
            TimeRadialWalkerTreeLayout(
                multiplyer = config.multiplayer,
                margin = config.margin.degreesToRadians(),
                rotation = config.rotation.degreesToRadians(),
                minCycleTimeDifference = config.minCycleTimeDifference,
                extendedPrefixAutomaton = config.extendedPrefixAutomaton,
                tree = root
            )
        }

        else -> {
            throw IllegalStateException("Wrong layout config provided. This shouldn't happen")
        }
    }

    /** Converts degrees to radians. */
    private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

    fun createLayout(layoutConfig: LayoutConfig, extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>): Layout {
        return when (layoutConfig) {
            is LayoutConfig.ClusteringLayoutConfig -> ClusteringLayout(
                extendedPrefixAutomaton,
                config = layoutConfig
            )

            else -> throw IllegalStateException("Wrong layout config provided. This shouldn't happen")
        }
    }
}