package moritz.lindner.masterarbeit.epa.features.lod

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

/** Interface for querying Level of Detail visibility */
interface LODQuery {
    /** Check if a state should be visible at current LOD level */
    fun isVisible(state: State): Boolean

    /**
     * Get opacity for smooth transitions (0.0 to 1.0) Returns 1.0 for fully
     * visible, 0.0 for invisible
     */
    fun getOpacity(state: State): Float
}
