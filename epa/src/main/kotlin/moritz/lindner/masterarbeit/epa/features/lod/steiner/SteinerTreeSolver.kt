package moritz.lindner.masterarbeit.epa.features.lod.steiner

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

class SteinerTreeSolver<T : Comparable<T>>(
    private val epa: ExtendedPrefixAutomaton<T>
) {

    /**
     * Computes the minimal Steiner tree connecting all terminal states.
     *
     * Since EPA is a tree structure (rooted at State.Root), this is optimal
     * and runs in O(|States|).
     *
     * @param terminals Set of states that must be connected
     * @return SteinerTreeResult containing the minimal connecting subtree
     */
    fun computeSteinerTree(terminals: Set<State>): SteinerLODLevel {
        // Edge cases
        if (terminals.isEmpty()) {
            return SteinerLODLevel(emptySet(), emptySet())
        }
        if (terminals.size == 1) {
            return SteinerLODLevel(terminals, emptySet())
        }

        val steinerStates = mutableSetOf<State>()
        val steinerTransitions = mutableSetOf<Transition>()
        val visited = mutableSetOf<State>()

        /**
         * DFS that returns true if the subtree rooted at this state contains any
         * terminal. Marks all states and transitions on paths between terminals.
         */
        fun dfs(state: State): Boolean {
            if (state in visited) return false
            visited.add(state)

            var hasTerminalInSubtree = state in terminals

            if (hasTerminalInSubtree) {
                steinerStates.add(state)
            }

            // Get all outgoing transitions from this state
            val outgoingTransitions = epa.outgoingTransitionsByState[state] ?: emptyList()

            for (transition in outgoingTransitions) {
                val childState = transition.end

                if (dfs(childState)) {
                    // Subtree contains a terminal, so include this state and transition
                    hasTerminalInSubtree = true
                    steinerStates.add(state)
                    steinerTransitions.add(transition)
                }
            }

            return hasTerminalInSubtree
        }

        dfs(State.Root)

        return SteinerLODLevel(
            terminals = terminals,
            steinerTreeNodes = steinerStates,
        )
    }
}