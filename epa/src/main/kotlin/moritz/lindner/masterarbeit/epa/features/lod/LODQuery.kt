package moritz.lindner.masterarbeit.epa.features.lod

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

/** Interface for querying Level of Detail visibility */
interface LODQuery {
    /** Check if a state should be visible at current LOD level */
    fun isVisible(state: State): Boolean
}
