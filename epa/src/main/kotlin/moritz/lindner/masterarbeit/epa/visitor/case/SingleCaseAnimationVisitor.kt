package moritz.lindner.masterarbeit.epa.visitor.case

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor
import java.util.TreeMap

class SingleCaseAnimationVisitor<T : Comparable<T>>(
    val caseIdentifier: String,
) : AutomataVisitor<T> {
    val stateAtTimestamp: TreeMap<T, State> = TreeMap()

    fun build(): CaseAnimation<T> = CaseAnimation(caseIdentifier, stateAtTimestamp, stateAtTimestamp.size)

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {
        val relevantEvent = extendedPrefixAutomata.sequence(state).find { it.caseIdentifier == caseIdentifier }
        if (relevantEvent != null) {
            val timestamp = relevantEvent.timestamp
            stateAtTimestamp[timestamp] = state
        }
    }
}
