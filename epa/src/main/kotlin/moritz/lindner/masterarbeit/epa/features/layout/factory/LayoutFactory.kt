package moritz.lindner.masterarbeit.epa.features.layout.factory

import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.PartitionClusteringLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.StateClusteringLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.parallelreadabletree.ParallelReadableTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.semantic.AngleSimilarityDepthTimeRadialLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.semantic.CycleTimeRadialLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.radial.semantic.PartitionSimilarityRadialLayout
import moritz.lindner.masterarbeit.epa.features.layout.tree.EPATreeNode
import moritz.lindner.masterarbeit.epa.features.layout.tree.EpaToTree
import kotlin.math.PI

/** Factory for creating TreeLayout instances based on configuration. */
object LayoutFactory {

    fun createLayout(
        config: LayoutConfig,
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        backgroundDispatcher: ExecutorCoroutineDispatcher,
        progressCallback: EpaProgressCallback? = null
    ): Layout {

        return when (config) {
            is LayoutConfig.WalkerConfig -> {
                WalkerTreeLayout(
                    distance = config.distance,
                    yDistance = config.layerSpace,
                    tree = buildTree(extendedPrefixAutomaton, progressCallback)
                )
            }

            is LayoutConfig.RadialWalkerConfig -> {


                RadialWalkerTreeLayout(
                    tree = buildTree(extendedPrefixAutomaton, progressCallback),
                    layerSpace = config.layerSpace,
                    margin = config.margin.degreesToRadians(),
                    rotation = config.rotation.degreesToRadians(),
                )
            }

            is LayoutConfig.DirectAngularConfig -> {
                DirectAngularPlacementTreeLayout(
                    tree = buildTree(extendedPrefixAutomaton, progressCallback),
                    layerSpace = config.layerSpace,
                    rotation = config.rotation.degreesToRadians()
                )
            }

            is LayoutConfig.CycleTimeRadialLayoutConfig -> {
                CycleTimeRadialLayout(
                    config = config,
                    extendedPrefixAutomaton = config.extendedPrefixAutomaton,
                    tree = buildTree(extendedPrefixAutomaton, progressCallback),
                )
            }

            is LayoutConfig.PartitionSimilarityRadialLayoutConfig -> {
                PartitionSimilarityRadialLayout(
                    extendedPrefixAutomaton = extendedPrefixAutomaton,
                    config = config,
                    backgroundDispatcher = backgroundDispatcher,
                )
            }

            is LayoutConfig.AngleSimilarityDepthTimeRadialLayoutConfig -> {
                AngleSimilarityDepthTimeRadialLayout(
                    extendedPrefixAutomaton = extendedPrefixAutomaton,
                    config = config,
                    backgroundDispatcher = backgroundDispatcher
                )
            }

            is LayoutConfig.StateClusteringLayoutConfig -> StateClusteringLayout(
                extendedPrefixAutomaton,
                config = config
            )

            is LayoutConfig.PRTLayoutConfig -> {
                ParallelReadableTreeLayout(
                    extendedPrefixAutomaton = extendedPrefixAutomaton,
                    config = config,
                    backgroundDispatcher = backgroundDispatcher,
                )
            }

            is LayoutConfig.PartitionClusteringLayoutConfig -> {
                PartitionClusteringLayout(
                    extendedPrefixAutomaton = extendedPrefixAutomaton,
                    config = config,
                    backgroundDispatcher = backgroundDispatcher,
                )
            }
        }
    }

    private fun buildTree(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        progressCallback: EpaProgressCallback?,
    ): EPATreeNode {
        val treeVisitor = EpaToTree<Long>(progressCallback)
        extendedPrefixAutomaton.acceptDepthFirst(treeVisitor)
        return treeVisitor.root
    }

    /** Converts degrees to radians. */
    fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f
}