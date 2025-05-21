package moritz.lindner.masterarbeit.epa.drawing.tree

import moritz.lindner.masterarbeit.epa.domain.State

/**
 * EPATreeNode represents a node (state) of the EPA as a tree structure.
 * It has a reference to its parent (null for root) and its children and
 * provides functionality to construct tree layouts efficiently.
 */
class EPATreeNode(
    val state: State,
    val parent: EPATreeNode? = null,
    val depth: Int,
) : Iterable<EPATreeNode> {
    private val children = mutableListOf<EPATreeNode>()

    // gets set when added from partent
    private var childIndex: Int = -1

    val leftSibling: EPATreeNode?
        get() =
            parent?.children?.let { siblings ->
                if (childIndex > 0) siblings[childIndex - 1] else null
            }

    val leftmostSibling: EPATreeNode?
        get() = parent?.children?.first()

    fun isLeaf(): Boolean = children.isEmpty()

    /**
     * adds the provided node to these nodes children and saves the resulting index nodeToAdd
     */
    fun addChild(nodeToAdd: EPATreeNode) {
        children.add(nodeToAdd)
        nodeToAdd.childIndex = children.size - 1
    }

    fun leftmostChild(): EPATreeNode? = children.firstOrNull()

    fun rightmostChild(): EPATreeNode? = children.lastOrNull()

    fun hasChildren(): Boolean = children.isNotEmpty()

    /**
     *  returns the index of the nodes in its parent’s child list.
     **/
    fun number(): Int = childIndex

    fun children(): List<EPATreeNode> = children

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
