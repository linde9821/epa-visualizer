package moritz.lindner.masterarbeit.epa.features.layout

import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle

// TODO: add ability to rotate coordinates after creation to avoid expensive recalculation
interface Layout: Iterable<NodePlacement>  {

    /**
     * Computes and assigns layout coordinates to each node in the tree.
     *
     * Must be called before querying layout-related data.
     */
    fun build(progressCallback: EpaProgressCallback? = null)

    /**
     * Indicates whether [build] has been successfully called.
     *
     * @return true if layout has been computed, false otherwise.
     */
    fun isBuilt(): Boolean

    /**
     * Returns the 2D coordinate for the given [moritz.lindner.masterarbeit.epa.domain.State] in the tree.
     *
     * @throws IllegalStateException if the layout has not been built yet.
     * @throws NoSuchElementException if the state is not part of the layout.
     */
    fun getCoordinate(state: State): Coordinate

    /**
     * Returns all node placements (with their coordinates) that lie within the
     * specified [rectangle].
     *
     * Useful for viewport queries or region-based selection.
     *
     * @param rectangle The rectangular area to query.
     * @return A list of [NodePlacement] for matching nodes.
     */
    fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacement>
}