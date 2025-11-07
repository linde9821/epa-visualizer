package moritz.lindner.masterarbeit.epa.features.layout.factory

import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.PRTLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.PartitionSimilarityRadialLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.TimeRadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.PartitionClusteringLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.StateClusteringLayout
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
    fun createTreeLayout(
        config: LayoutConfig,
        root: EPATreeNode,
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>
    ): TreeLayout = when (config) {
        is LayoutConfig.WalkerConfig -> {
            WalkerTreeLayout(
                distance = config.distance,
                yDistance = config.layerSpace,
                tree = root
            )
        }

        is LayoutConfig.RadialWalkerConfig -> {
            RadialWalkerTreeLayout(
                tree = root,
                layerSpace = config.layerSpace,
                margin = config.margin.degreesToRadians(),
                rotation = config.rotation.degreesToRadians(),
            )
        }

        is LayoutConfig.DirectAngularConfig -> {
            DirectAngularPlacementTreeLayout(
                tree = root,
                layerSpace = config.layerSpace,
                rotation = config.rotation.degreesToRadians()
            )
        }

        is LayoutConfig.RadialWalkerTimeConfig -> {
            TimeRadialWalkerTreeLayout(
                multiplier = config.layerBaseUnit,
                margin = config.margin.degreesToRadians(),
                rotation = config.rotation.degreesToRadians(),
                minCycleTimeDifference = config.minCycleTimeDifference,
                extendedPrefixAutomaton = config.extendedPrefixAutomaton,
                tree = root
            )
        }

        is LayoutConfig.PartitionSimilarityRadialLayoutConfig -> {
            PartitionSimilarityRadialLayout(
                extendedPrefixAutomaton = extendedPrefixAutomaton,
                config = config
            )
        }

        else -> {
            throw IllegalStateException("Wrong layout config provided. This shouldn't happen")
        }
    }

    /** Converts degrees to radians. */
    private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

    fun createLayout(
        layoutConfig: LayoutConfig,
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        backgroundDispatcher: ExecutorCoroutineDispatcher
    ): Layout {
        return when (layoutConfig) {
            is LayoutConfig.StateClusteringLayoutConfig -> StateClusteringLayout(
                extendedPrefixAutomaton,
                config = layoutConfig
            )

            is LayoutConfig.PRTLayoutConfig -> {
                PRTLayout(
                    extendedPrefixAutomaton = extendedPrefixAutomaton,
                    config = layoutConfig,
                    backgroundDispatcher = backgroundDispatcher,
                )
            }

            is LayoutConfig.PartitionClusteringLayoutConfig -> {
                PartitionClusteringLayout(
                    extendedPrefixAutomaton = extendedPrefixAutomaton,
                    config = layoutConfig
                )
            }

            else -> throw IllegalStateException("Wrong layout config provided. This shouldn't happen")
        }
    }
}