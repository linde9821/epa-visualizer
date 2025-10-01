package moritz.lindner.masterarbeit.epa.features.layout

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import moritz.lindner.masterarbeit.epa.features.layout.tree.EPATreeNode

// TODO: add ability to rotate coordinates after creation to avoid expensive recalculation
/**
 * Defines a layout strategy for positioning nodes in a tree structure derived from an EPA.
 *
 * Implementations of this interface compute and provide access to spatial information
 * (e.g., coordinates) for visualizing or analyzing an [EPATreeNode]-based structure.
 */
interface TreeLayout : Iterable<NodePlacement> {
    /**
     * Computes and assigns layout coordinates to each node in the tree.
     *
     * Must be called before querying layout-related data.
     *
     * @param tree The root node of the tree to layout.
     */
    fun build(tree: EPATreeNode)

    /**
     * Returns the 2D coordinate for the given [State] in the tree.
     *
     * @throws IllegalStateException if the layout has not been built yet.
     * @throws NoSuchElementException if the state is not part of the layout.
     */
    fun getCoordinate(state: State): Coordinate

    /**
     * Returns all node placements (with their coordinates) that lie within the specified [rectangle].
     *
     * Useful for viewport queries or region-based selection.
     *
     * @param rectangle The rectangular area to query.
     * @return A list of [NodePlacement] for matching nodes.
     */
    fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacement>

    /**
     * Returns the maximum depth (i.e., longest path from root) of the laid-out tree.
     *
     * @return The maximum depth as an integer.
     */
    fun getMaxDepth(): Int

    /**
     * Indicates whether [build] has been successfully called.
     *
     * @return true if layout has been computed, false otherwise.
     */
    fun isBuilt(): Boolean
}
