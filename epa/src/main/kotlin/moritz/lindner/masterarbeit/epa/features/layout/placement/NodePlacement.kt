package moritz.lindner.masterarbeit.epa.features.layout.placement

import moritz.lindner.masterarbeit.epa.features.layout.tree.EPATreeNode

/**
 * Associates a node in the EPA tree with a specific coordinate in a 2D
 * layout.
 *
 * @property coordinate The 2D coordinate assigned to the node.
 * @property node The tree node being placed.
 */
data class NodePlacement(
    val coordinate: Coordinate,
    val node: EPATreeNode,
)
