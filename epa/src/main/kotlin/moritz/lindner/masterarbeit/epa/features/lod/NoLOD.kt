package moritz.lindner.masterarbeit.epa.features.lod

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

/** Default implementation that shows everything (no LOD) */
class NoLOD : LODQuery {
    override fun isVisible(state: State) = true
    override fun isVisible(transition: Transition) = true
    override fun getOpacity(state: State) = 1f
    override fun getAggregationInfo(state: State): AggregationInfo? = null
}