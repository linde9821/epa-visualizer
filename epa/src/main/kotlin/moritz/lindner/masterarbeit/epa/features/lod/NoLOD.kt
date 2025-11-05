package moritz.lindner.masterarbeit.epa.features.lod

import moritz.lindner.masterarbeit.epa.domain.State

class NoLOD : LODQuery {
    override fun isVisible(state: State) = true
}