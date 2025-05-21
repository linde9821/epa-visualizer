package moritz.lindner.masterarbeit.epa.drawing.tree

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

class TreeBuildingVisitor<T : Comparable<T>> : AutomataVisitor<T> {
    lateinit var root: EPATreeNode
        private set

    private val stateToNode = HashMap<State, EPATreeNode>()
    private var currentState: State? = null

    override fun onStart(extendedPrefixAutomata: ExtendedPrefixAutomata<T>) {
        val rootNode = EPATreeNode(State.Root, null, 0)
        root = rootNode
        stateToNode[State.Root] = rootNode
        currentState = State.Root
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {
        currentState = state
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        transition: Transition,
        depth: Int,
    ) {
        val parentNode =
            stateToNode[transition.start]
                ?: throw IllegalStateException("No node for parent state: ${transition.start}")

        val childNode =
            EPATreeNode(
                state = transition.end,
                parent = parentNode,
                depth = depth + 1,
            )

        parentNode.addChild(childNode)
        stateToNode[transition.end] = childNode
    }
}
