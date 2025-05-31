package moritz.lindner.masterarbeit.epa.drawing.layout

/**
 * A specialized [TreeLayout] that arranges tree nodes in a radial (circular) fashion.
 *
 * Nodes are positioned based on their depth and angle, with the root in the center
 * and children placed on concentric circles around it.
 */
interface RadialTreeLayout : TreeLayout {
    /**
     * Returns the radius of the outermost circle that contains the deepest nodes.
     *
     * This value can be used for rendering boundaries or scaling calculations.
     *
     * @return The radius in layout units.
     */
    fun getCircleRadius(): Float
}
