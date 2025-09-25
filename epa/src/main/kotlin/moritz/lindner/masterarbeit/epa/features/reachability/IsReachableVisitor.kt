package moritz.lindner.masterarbeit.epa.features.reachability

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class IsReachableVisitor<T : Comparable<T>>(
    private val statesToCheckReach: Set<State>,
    private val onProgressCallback: EpaProgressCallback? = null
) : AutomatonVisitor<T> {

    private var isBuilt = false
    private val reachableStates = mutableSetOf<State>()

    fun isReachable(state: State): Boolean {
        if (!isBuilt) throw IllegalStateException("Visitor has not been built yet")
        if (state !in statesToCheckReach) throw IllegalStateException("State $state was not in set of states to check")

        return when (state) {
            is State.PrefixState -> state in reachableStates
            State.Root -> true
        }
    }

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        isBuilt = true
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int
    ) {
        if (state in statesToCheckReach) {
            reachableStates.add(state)
        }
    }

    override fun onProgress(current: Long, total: Long) {
        onProgressCallback?.onProgress(current, total, "Reachability")
    }
}