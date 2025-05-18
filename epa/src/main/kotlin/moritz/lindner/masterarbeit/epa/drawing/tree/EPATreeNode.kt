package moritz.lindner.masterarbeit.epa.drawing.tree

import moritz.lindner.masterarbeit.epa.domain.State

/**
 * EPATreeNode represents a node (state) of the EPA as a tree structure.
 * It has a reference to its parent (null for root) and its children and
 * provides functionality to construct tree layouts efficiently.
 */
class EPATreeNode<T : Comparable<T>>(
    val state: State,
    val parent: EPATreeNode<T>? = null,
    val depth: Int,
) : Iterable<EPATreeNode<T>> {
    private val children = mutableListOf<EPATreeNode<T>>()

    // gets set when added
    private var childIndex: Int = -1

    val leftSibling: EPATreeNode<T>?
        get() =
            parent?.children?.let { siblings ->
                val index = siblings.indexOf(this)
                if (index > 0) siblings[index - 1] else null
            }

    val leftmostSibling: EPATreeNode<T>?
        get() = parent?.children?.first()

    fun isLeaf(): Boolean = children.isEmpty()

    /**
     * adds the provided node to these nodes children and saves the resulting index nodeToAdd
     */
    fun addChild(nodeToAdd: EPATreeNode<T>) {
        children.add(nodeToAdd)
        nodeToAdd.childIndex = children.size - 1
    }

    fun leftmostChild(): EPATreeNode<T>? = children.firstOrNull()

    fun rightmostChild(): EPATreeNode<T>? = children.lastOrNull()

    fun hasChildren(): Boolean = children.isNotEmpty()

    /**
     *  returns the index of the nodes in its parent’s child list.
     *  */
    fun number(): Int = childIndex

    fun children(): List<EPATreeNode<T>> = children

    override fun iterator(): Iterator<EPATreeNode<T>> =
        object : Iterator<EPATreeNode<T>> {
            private val stack = ArrayDeque<EPATreeNode<T>>().apply { add(this@EPATreeNode) }

            override fun hasNext(): Boolean = stack.isNotEmpty()

            override fun next(): EPATreeNode<T> {
                val node = stack.removeLast()
                // Add children in reverse to process left-to-right
                node.children.asReversed().forEach { stack.addLast(it) }
                return node
            }
        }
}
