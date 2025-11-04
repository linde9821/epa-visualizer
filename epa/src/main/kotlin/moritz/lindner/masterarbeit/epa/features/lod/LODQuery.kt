package moritz.lindner.masterarbeit.epa.features.lod

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

/** Interface for querying Level of Detail visibility */
interface LODQuery {
    /** Check if a state should be visible at current LOD level */
    fun isVisible(state: State): Boolean

    /** Check if a transition should be visible at current LOD level */
    fun isVisible(transition: Transition): Boolean

    /**
     * Get opacity for smooth transitions (0.0 to 1.0) Returns 1.0 for fully
     * visible, 0.0 for invisible
     */
    fun getOpacity(state: State): Float

    /** Get opacity for transition */
    fun getOpacity(transition: Transition): Float

    /** Get aggregation info if this state represents aggregated children */
    fun getAggregationInfo(state: State): AggregationInfo?
}

data class AggregationInfo(
    val hiddenChildCount: Int,
    val hiddenPartitions: Set<Int>,
    val totalEventCount: Int
)

/** Default implementation that shows everything (no LOD) */
class NoLOD : LODQuery {
    override fun isVisible(state: State) = true
    override fun isVisible(transition: Transition) = true
    override fun getOpacity(state: State) = 1f
    override fun getOpacity(transition: Transition) = 1f
    override fun getAggregationInfo(state: State): AggregationInfo? = null
}
