package moritz.lindner.masterarbeit.epa.visitor

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

/**
 * A visitor interface for traversing an [ExtendedPrefixAutomaton] and its components.
 *
 * The traversal follows a consistent pattern:
 * 1. Visit the [State]
 * 2. Visit all [Event]s associated with that state
 * 3. Visit all outgoing [Transition]s from that state
 *
 * Implementations can choose to traverse the automaton in either depth-first or breadth-first order,
 * and override whichever methods they need.
 *
 * @param T The type of timestamp used in the automaton's events.
 */
interface AutomatonVisitor<T : Comparable<T>> {
    /**
     * Called once at the beginning of the traversal.
     *
     * @param extendedPrefixAutomaton The automaton being traversed.
     */
    fun onStart(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
    }

    /**
     * Called once at the end of the traversal.
     *
     * @param extendedPrefixAutomaton The automaton being traversed.
     */
    fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
    }

    /**
     * Called when visiting a [State] in the automaton.
     *
     * @param extendedPrefixAutomaton The automaton being traversed.
     * @param state The current state being visited.
     * @param depth The current depth in the traversal tree.
     */
    fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int,
    ) {
    }

    /**
     * Called when visiting a [Transition] from the current state.
     *
     * @param extendedPrefixAutomaton The automaton being traversed.
     * @param transition The transition being visited.
     * @param depth The current depth in the traversal tree.
     */
    fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        transition: Transition,
        depth: Int,
    ) {
    }

    /**
     * Called when visiting an [Event] associated with the current state.
     *
     * @param extendedPrefixAutomaton The automaton being traversed.
     * @param event The event being visited.
     * @param depth The current depth in the traversal tree.
     */
    fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        event: Event<T>,
        depth: Int,
    ) {
    }

    /**
     * Called to report traversal progress, if applicable.
     *
     * @param current The number of elements processed so far.
     * @param total The total number of elements to process.
     */
    fun onProgress(
        current: Long,
        total: Long,
    ) {
    }
}
