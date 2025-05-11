package moritz.lindner.masterarbeit.epa.visitor

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

interface AutomataVisitor<T : Comparable<T>> {

    fun onStart(extendedPrefixAutomata: ExtendedPrefixAutomata<T>) {

    }

    fun onEnd(extendedPrefixAutomata: ExtendedPrefixAutomata<T>) {

    }

    fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {

    }

    fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        transition: Transition,
        depth: Int,
    ) {

    }

    fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        event: Event<T>,
        depth: Int,
    ) {

    }

    fun onProgress(
        current: Long,
        total: Long,
    ) {
    }
}
