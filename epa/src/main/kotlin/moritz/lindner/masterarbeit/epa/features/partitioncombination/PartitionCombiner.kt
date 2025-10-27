package moritz.lindner.masterarbeit.epa.features.partitioncombination

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

class PartitionCombiner<T : Comparable<T>> : AutomatonVisitor<T> {

    private lateinit var statePartitionsCollection: StatePartitionsCollection<T>
    private var isFinished = false

    override fun onStart(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        isFinished = false
        statePartitionsCollection = StatePartitionsCollection(extendedPrefixAutomaton)
    }

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        isFinished = true
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int
    ) {
        statePartitionsCollection.addStateAndUpdateAllOtherPartitions(state)
    }

    fun getStatePartitions(): StatePartitionsCollection<T> {
        require(isFinished)
        return statePartitionsCollection
    }
}