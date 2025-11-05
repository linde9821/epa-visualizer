package moritz.lindner.masterarbeit.epa.features.lod.steiner

import moritz.lindner.masterarbeit.epa.domain.State

data class SteinerLODLevel(
    val terminals: Set<State>,
    val steinerTreeNodes: Set<State>,
)