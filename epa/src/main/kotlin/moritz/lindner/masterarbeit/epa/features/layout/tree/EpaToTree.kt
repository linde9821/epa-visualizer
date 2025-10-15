package moritz.lindner.masterarbeit.epa.features.layout.tree

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

/**
 * A visitor that constructs a tree representation of an
 * [ExtendedPrefixAutomaton], mapping each [State] to an [EPATreeNode] with
 * parent/child relationships.
 *
 * The tree is rooted at [State.Root] and mirrors the traversal order. Must
 * be used with [ExtendedPrefixAutomaton.acceptDepthFirst] to ensure a
 * valid parent-before-child visit order.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class EpaToTree<T : Comparable<T>>(private val progressCallback: EpaProgressCallback? = null) : AutomatonVisitor<T> {
    /**
     * The root node of the constructed tree. Will represent [State.Root].
     * Populated after [onStart] is invoked by the traversal.
     */
    lateinit var root: EPATreeNode
        private set

    private val stateToNode = HashMap<State, EPATreeNode>()
    private var currentState: State? = null

    override fun onProgress(current: Long, total: Long) {
        progressCallback?.onProgress(current, total, "Build Tree representation of epa")
    }

    override fun onStart(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {
        val rootNode = EPATreeNode(State.Root, null, 0)
        root = rootNode
        stateToNode[State.Root] = rootNode
        currentState = State.Root
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        depth: Int,
    ) {
        currentState = state
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
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
