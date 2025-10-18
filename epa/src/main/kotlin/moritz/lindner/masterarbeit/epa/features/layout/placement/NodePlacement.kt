package moritz.lindner.masterarbeit.epa.features.layout.placement

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.tree.EPATreeNode

/**
 * Associates a node in the EPA tree with a specific coordinate in a 2D
 * layout.
 *
 * @property coordinate The 2D coordinate assigned to the node.
 * @property state The state being placed.
 */
data class NodePlacement(
    val coordinate: Coordinate,
    val state: State,
)
