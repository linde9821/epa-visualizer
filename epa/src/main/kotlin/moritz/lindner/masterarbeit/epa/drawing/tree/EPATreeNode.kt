package moritz.lindner.masterarbeit.epa.drawing.tree

import moritz.lindner.masterarbeit.epa.domain.State

/**
 * Represents a node in a tree-based layout of an [ExtendedPrefixAutomata] state.
 *
 * Each node wraps a [State], maintains a reference to its parent (if any), and stores
 * its child nodes. It also supports left/right sibling access and depth-first traversal.
 *
 * @property state The EPA [State] this node represents.
 * @property parent The parent node in the tree (null for the root).
 * @property depth The depth of the node in the tree (root = 0).
 */
class EPATreeNode(
    val state: State,
    val parent: EPATreeNode? = null,
    val depth: Int,
) : Iterable<EPATreeNode> {
    private val children = mutableListOf<EPATreeNode>()

    // Set when the node is added to a parent
    private var childIndex: Int = -1

    /**
     * Returns the immediate left sibling of this node, or null if it is the first child.
     */
    val leftSibling: EPATreeNode?
        get() =
            parent?.children?.let { siblings ->
                if (childIndex > 0) siblings[childIndex - 1] else null
            }

    /**
     * Returns the leftmost sibling of this node (the first child of the parent), or null if root.
     */
    val leftmostSibling: EPATreeNode?
        get() = parent?.children?.first()

    /**
     * Returns true if this node has no children.
     */
    fun isLeaf(): Boolean = children.isEmpty()

    /**
     * Adds a child node to this node and records the child's index within the list.
     *
     * @param nodeToAdd The child node to add.
     */
    fun addChild(nodeToAdd: EPATreeNode) {
        children.add(nodeToAdd)
        nodeToAdd.childIndex = children.size - 1
    }

    /**
     * Returns the leftmost child node, or null if there are no children.
     */
    fun leftmostChild(): EPATreeNode? = children.firstOrNull()

    /**
     * Returns the rightmost child node, or null if there are no children.
     */
    fun rightmostChild(): EPATreeNode? = children.lastOrNull()

    /**
     * Returns true if the node has one or more children.
     */
    fun hasChildren(): Boolean = children.isNotEmpty()

    /**
     * Returns this node's index in its parent’s child list (0-based).
     */
    fun number(): Int = childIndex

    /**
     * Returns the list of this node's children.
     */
    fun children(): List<EPATreeNode> = children

    /**
     * Provides a depth-first iterator over this node and its descendants.
     */
    override fun iterator(): Iterator<EPATreeNode> =
        object : Iterator<EPATreeNode> {
            private val stack = ArrayDeque<EPATreeNode>().apply { add(this@EPATreeNode) }

            override fun hasNext(): Boolean = stack.isNotEmpty()

            override fun next(): EPATreeNode {
                val node = stack.removeLast()
                // Add children in reverse to process left-to-right
                node.children.asReversed().forEach { stack.addLast(it) }
                return node
            }
        }
}
