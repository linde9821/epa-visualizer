package epa

import moritz.lindner.masterarbeit.epa.tree.EPATreeNode

class TreeLayout<T : Comparable<T>>(
    private val tree: EPATreeNode<T>,
    private val distance: Double,
) {
    private val threads = mutableMapOf<EPATreeNode<T>, Double>()
    private val modifiers = mutableMapOf<EPATreeNode<T>, Double>()
    private val ancestor = mutableMapOf<EPATreeNode<T>, List<EPATreeNode<T>>>()
    private val prelim = mutableMapOf<EPATreeNode<T>, Double>()

    fun build() {
        // for all nodes v of T
        tree.forEach { v ->
            // let mod(v) = thread(v) = 0
            modifiers[v] = 0.0
            threads[v] = 0.0
            // let ancestor (v) = v
            ancestor[v] = listOf(v)
        }

        // let r be the root of T
        val r = tree

        // FirstWalk(r)
        firstWalk(r)
        // SecondWalk(r, −prelim(r))
        secondWalk(r, -prelim[r]!!)
    }

    private fun firstWalk(v: EPATreeNode<T>) {
        // if v is a leaf
        if (v.isLeaf()) {
            // let prelim(v) = 0
            prelim[v] = 0.0

            // if v has a left sibling w
            val w = v.leftSibling
            if (w != null) {
                // let prelim(v) = prelim(w) + distance
                prelim[v] =
                    prelim[w]!! + distance // here distance can be made variable distance(v, w) a function of the widths of v and w
            }
        } else { // else
            // let defaultAncestor be the leftmost child of v
            val defaultAncestor = v.leftmostChild()!!

            // for all children w of v from left to right
            v.children.forEach { w ->
                // FirstWalk(w)
                firstWalk(w)

                // Apportion(w,defaultAncestor)
                apportion(w, defaultAncestor)
            }

            // ExecuteShifts(v)
            executeShifts(v)

            // let midpoint = (1/2) (prelim(leftmost child of v) + prelim(rightmost child of v))
            val midpoint = 0.5 * (prelim[v.leftmostChild()!!]!! + prelim[v.rightmostChild()!!]!!)

            // if v has a left sibling w
            val w = v.leftSibling
            if (w != null) {
                // let prelim(v) = prelim(w) + distance
                prelim[v] = prelim[w]!! + distance
                // let mod(v) = prelim(v) − midpoint
                modifiers[v] = prelim[v]!! - midpoint
            } else { // else
                prelim[v] = midpoint
            }
        }
    }

    private fun apportion(
        v: EPATreeNode<T>,
        defaultAncestor: EPATreeNode<T>,
    ) {
        // if v has a left sibling w
        val w = v.leftSibling
        if (w != null) {
            // let vi+ = vo+ = v
            val viPlus = v
            val voPlus = v
            // let viMinus = w
            val viMinus = w
            // let voMinus be the leftmost sibling of viPlus
            val voMinus = viPlus.leftmostSibling // continue here
        }
    }

    private fun executeShifts(v: EPATreeNode<T>) {
        TODO("Not yet implemented")
    }

    private fun secondWalk(
        r: EPATreeNode<T>,
        foo: Double,
    ) {
    }
}
