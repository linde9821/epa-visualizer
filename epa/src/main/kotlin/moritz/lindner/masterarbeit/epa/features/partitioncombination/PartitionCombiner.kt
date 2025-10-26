package moritz.lindner.masterarbeit.epa.features.partitioncombination

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class PartitionCombiner<T : Comparable<T>> : AutomatonVisitor<T> {

    private val statePartitionsCollection = StatePartitionsCollection<T>()
    private var isFinished = false

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        isFinished = true
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int
    ) {
        statePartitionsCollection.addStateAndUpdateAllOtherPartitions(extendedPrefixAutomaton, state)
    }

    fun getStatePartitions(): StatePartitionsCollection<T> {
        require(isFinished)
        return statePartitionsCollection
    }
}