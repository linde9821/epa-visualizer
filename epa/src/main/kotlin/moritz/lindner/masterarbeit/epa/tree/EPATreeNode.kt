package moritz.lindner.masterarbeit.epa.tree

import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

class EPATreeNode<T : Comparable<T>>(
    val state: State,
    val transitionFromParent: Transition?, // is null for root state
    val parent: EPATreeNode<T>? = null,
    val sequence: Set<Event<T>>,
    val level: Int,
) : Iterable<EPATreeNode<T>> {
    // todo: check datastructure
    private val children = mutableListOf<EPATreeNode<T>>()
    private var childIndex: Int = -1

    val leftSibling: EPATreeNode<T>?
        get() =
            parent?.children?.let { siblings ->
                val index = siblings.indexOf(this)
                if (index > 0) siblings[index - 1] else null
            }

    val rightSibling: EPATreeNode<T>?
        get() =
            parent?.children?.let { siblings ->
                val index = siblings.indexOf(this)
                if (index >= 0 && index < siblings.size - 1) siblings[index + 1] else null
            }

    val leftmostSibling: EPATreeNode<T>?
        get() = parent?.children?.first()

    fun isLeaf(): Boolean = children.isEmpty()

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

    fun addChild(node: EPATreeNode<T>) {
        children.add(node)
        node.childIndex = children.size
    }

    fun leftmostChild(): EPATreeNode<T>? = children.firstOrNull()

    fun rightmostChild(): EPATreeNode<T>? = children.lastOrNull()

    fun hasChildren(): Boolean = children.isNotEmpty()

    // number() gives the index of node w in its parent’s child list.
    fun number(): Int = childIndex

    fun children(): List<EPATreeNode<T>> = children.toList()
}
