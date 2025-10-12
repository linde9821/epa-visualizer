package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing

import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

interface AtlasConfig {
    fun toStateAtlasEntry(state: State): StateAtlasEntry
    fun toTransitionAtlasEntry(transition: Transition): TransitionAtlasEntry
}