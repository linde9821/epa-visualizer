package moritz.lindner.masterarbeit.epa.tree

import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

class EPATreeNode<T : Comparable<T>>(
    val state: State,
    val transitionFromParent: Transition?, // is null for root state
    val parent: EPATreeNode<T>? = null,
    val sequence: Set<Event<T>>
) {
    // todo: check datastructure
    val children = mutableListOf<EPATreeNode<T>>()

    val leftSibling: EPATreeNode<T>?
        get() = parent?.children?.let { siblings ->
            val index = siblings.indexOf(this)
            if (index > 0) siblings[index - 1] else null
        }

    val rightSibling: EPATreeNode<T>?
        get() = parent?.children?.let { siblings ->
            val index = siblings.indexOf(this)
            if (index >= 0 && index < siblings.size - 1) siblings[index + 1] else null
        }

    fun isLeaf(): Boolean {
        return children.isEmpty()
    }

}