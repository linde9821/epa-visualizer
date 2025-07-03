package moritz.lindner.masterarbeit.epa.layout.tree

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

/**
 * A visitor that constructs a tree representation of an [ExtendedPrefixAutomata],
 * mapping each [State] to an [EPATreeNode] with parent/child relationships.
 *
 * The tree is rooted at [State.Root] and mirrors the traversal order.
 * Must be used with [ExtendedPrefixAutomata.acceptDepthFirst] to ensure a valid parent-before-child visit order.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class EpaToTree<T : Comparable<T>> : AutomataVisitor<T> {
    /**
     * The root node of the constructed tree. Will represent [State.Root].
     * Populated after [onStart] is invoked by the traversal.
     */
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
