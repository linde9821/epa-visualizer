package moritz.lindner.masterarbeit.epa.features.layout

import moritz.lindner.masterarbeit.epa.features.layout.tree.EPATreeNode


// TODO: add ability to rotate coordinates after creation to avoid expensive recalculation
/**
 * Defines a layout strategy for positioning nodes in a tree structure
 * derived from an EPA.
 *
 * Implementations of this interface compute and provide access to spatial
 * information (e.g., coordinates) for visualizing or analyzing an
 * [EPATreeNode]-based structure.
 */
interface TreeLayout : Layout {
    /**
     * Returns the maximum depth (i.e., longest path from root) of the laid-out
     * tree.
     *
     * @return The maximum depth as an integer.
     */
    fun getMaxDepth(): Int
}
