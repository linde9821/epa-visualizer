package moritz.lindner.masterarbeit.epa.features.subtree

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class SubtreeSizeVisitor: AutomatonVisitor<Long> {

    val sizeByState = mutableMapOf<State, Int>()

    val epaService = EpaService<Long>()

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        state: State,
        depth: Int
    ) {
        epaService.getPathFromRoot(state).forEach { state ->
            sizeByState[state] = sizeByState.getOrDefault(state, 0) + 1
        }
    }
}