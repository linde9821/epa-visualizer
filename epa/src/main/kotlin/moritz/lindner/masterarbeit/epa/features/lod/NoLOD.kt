package moritz.lindner.masterarbeit.epa.features.lod

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

class NoLOD : LODQuery {
    override fun isVisible(state: State) = true
    override fun getOpacity(state: State) = 1f
}