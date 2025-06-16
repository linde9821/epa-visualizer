package moritz.lindner.masterarbeit.epa.visitor.animation

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor
import java.util.TreeMap

class WholeCaseAnimationVisitor<T : Comparable<T>>(
    val caseIdentifier: String,
) : AutomataVisitor<T> {
    val stateAtTimestamp: TreeMap<T, List<State>> = TreeMap()

    fun build(): EventLogAnimation<T> = EventLogAnimation(caseIdentifier, stateAtTimestamp, stateAtTimestamp.size)

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {
        extendedPrefixAutomata
            .sequence(state)
            .forEach { event ->
                stateAtTimestamp[event.timestamp] = stateAtTimestamp[event.timestamp].orEmpty() + state
            }
    }
}
