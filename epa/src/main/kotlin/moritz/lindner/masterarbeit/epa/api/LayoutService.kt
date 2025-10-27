package moritz.lindner.masterarbeit.epa.api

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutFactory
import moritz.lindner.masterarbeit.epa.features.layout.tree.EpaToTree

/**
 * Service for generating visual layouts from Extended Prefix Automatons.
 *
 * @param T The type of timestamps used in the event log.
 */
class LayoutService<T : Comparable<T>>(
    private val backgroundDispatcher: ExecutorCoroutineDispatcher,
) {

    private val logger = KotlinLogging.logger {}

    /**
     * Builds a tree layout from an EPA using the specified layout
     * configuration.
     *
     * @param epa The Extended Prefix Automaton to layout.
     * @param layoutConfig Configuration specifying the layout algorithm and
     *    parameters.
     * @return A TreeLayout with positioned nodes for visualization.
     */
    // TODO: maybe add caching
    fun buildLayout(
        epa: ExtendedPrefixAutomaton<Long>,
        layoutConfig: LayoutConfig,
        progressCallback: EpaProgressCallback? = null
    ): Layout {
        val treeVisitor = EpaToTree<Long>(progressCallback)
        if (layoutConfig.enabled) {
            epa.acceptDepthFirst(treeVisitor)
            logger.info { "building tree layout" }
            val layout = createLayout(layoutConfig, epa, treeVisitor)

            layout.build(progressCallback)

            return layout
        } else return createLayout(layoutConfig, epa, treeVisitor)
    }

    private fun createLayout(
        layoutConfig: LayoutConfig,
        epa: ExtendedPrefixAutomaton<Long>,
        treeVisitor: EpaToTree<Long>
    ): Layout {
        return when (layoutConfig) {
            is LayoutConfig.StateClusteringLayoutConfig -> LayoutFactory.createLayout(
                layoutConfig,
                epa,
                backgroundDispatcher
            )

            is LayoutConfig.PartitionClusteringLayoutConfig -> LayoutFactory.createLayout(
                layoutConfig,
                epa,
                backgroundDispatcher
            )

            is LayoutConfig.PRTLayoutConfig -> LayoutFactory.createLayout(layoutConfig, epa, backgroundDispatcher)
            else -> LayoutFactory.createTreeLayout(layoutConfig, treeVisitor.root, epa)
        }
    }
}

