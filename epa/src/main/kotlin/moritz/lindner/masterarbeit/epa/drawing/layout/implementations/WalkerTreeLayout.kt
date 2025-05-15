package moritz.lindner.masterarbeit.epa.drawing.layout.implementations

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.drawing.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.drawing.placement.Coordinate
import moritz.lindner.masterarbeit.epa.drawing.placement.NodePlacementInformation
import moritz.lindner.masterarbeit.epa.drawing.placement.Rectangle
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode
import kotlin.math.max
import kotlin.math.min

open class WalkerTreeLayout<T : Comparable<T>>(
    private val distance: Float,
    private val yDistance: Float,
    expectedCapacity: Int = 1000,
) : TreeLayout<T> {
    private val logger = KotlinLogging.logger {}

    private val threads = HashMap<EPATreeNode<T>, EPATreeNode<T>?>(expectedCapacity)
    private val modifiers = HashMap<EPATreeNode<T>, Float>(expectedCapacity)
    private val ancestor = HashMap<EPATreeNode<T>, EPATreeNode<T>>(expectedCapacity)
    private val prelim = HashMap<EPATreeNode<T>, Float>(expectedCapacity)
    private val shifts = HashMap<EPATreeNode<T>, Float>(expectedCapacity)
    private val changes = HashMap<EPATreeNode<T>, Float>(expectedCapacity)
    protected val nodePlacementInformationByState = HashMap<State, NodePlacementInformation<T>>(expectedCapacity)

    private var maxDepth = Int.MIN_VALUE

    private var isBuilt = false

    protected var xMin = Float.MAX_VALUE
    protected var xMax = Float.MIN_VALUE

    private fun firstWalk(v: EPATreeNode<T>) {
        // if v is a leaf
        if (v.isLeaf()) {
            // let prelim(v) = 0
            prelim[v] = 0.0f

            // if v has a left sibling w
            val w = v.leftSibling
            if (w != null) {
                // let prelim(v) = prelim(w) + distance
                prelim[v] = prelim[w]!! + adjustedDistance(v, w)
            }
        } else { // else
            // let defaultAncestor be the leftmost child of v
            var defaultAncestor = v.leftmostChild()!!

            // for all children w of v from left to right
            v.children().forEach { w ->
                // FirstWalk(w)
                firstWalk(w)

                // Apportion(w,defaultAncestor)
                apportion(w, defaultAncestor)?.let { newDefaultAncestor ->
                    defaultAncestor = newDefaultAncestor
                }
            }

            // ExecuteShifts(v)
            executeShifts(v)

            // let midpoint = (1/2) (prelim(leftmost child of v) + prelim(rightmost child of v))
            val midpoint = 0.5f * (prelim[v.leftmostChild()!!]!! + prelim[v.rightmostChild()!!]!!)

            // if v has a left sibling w
            val w = v.leftSibling
            if (w != null) {
                // let prelim(v) = prelim(w) + distance
                prelim[v] = prelim[w]!! + adjustedDistance(v, w)
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
    ): EPATreeNode<T>? {
        // if v has a left sibling w
        val w = v.leftSibling
        if (w != null) {
            // let vi+ = vo+ = v
            var viPlus: EPATreeNode<T> = v
            var voPlus: EPATreeNode<T> = v
            // let viMinus = w
            var viMinus: EPATreeNode<T> = w
            // let voMinus be the leftmost sibling of viPlus
            var voMinus: EPATreeNode<T> = viPlus.leftmostSibling!!
            // let siPlus = mod(viPlus)
            var siPlus = modifiers[viPlus]!!
            // let soPlus = mod(voPlus)
            var soPlus = modifiers[voPlus]!!
            // let siMinus = mod(viMinus)
            var siMinus = modifiers[viMinus]!!
            // let soMinus = mod(voMinus)
            var soMinus = modifiers[voMinus]!!

            // while NextRight(viMinus) != 0 and NextLeft(viPlus) != 0
            while (nextRight(viMinus) != null && nextLeft(viPlus) != null) {
                // let viMinus = NextRight(viMinus)
                viMinus = nextRight(viMinus)!!
                // let viPlus = NextLeft(viPlus)
                viPlus = nextLeft(viPlus)!!
                // let voMinus = NextLeft(voMinus)
                voMinus = nextLeft(voMinus)!!
                // let voPLus = NextRight(v0PLus)
                voPlus = nextRight(voPlus)!!
                // let ancestor(voPlus) = v
                ancestor[voPlus] = v
                // let shift = (prelim(viMinus) + siMinus) - (prelim(viPlus) + siPlus) + distance
                val shift =
                    (prelim[viMinus]!! + siMinus) - (prelim[viPlus]!! + siPlus) + adjustedDistance(viMinus, viPlus)
                // if shift > 0
                if (shift > 0) {
                    // MoveSubtree(Ancestor(viMinus, v, defaultAncestor), v , shift)
                    moveSubtree(ancestor(viMinus, v, defaultAncestor), v, shift)
                    // let siPlus = siPlus + shift
                    siPlus += shift
                    // let soPLus = soPLus + shift
                    soPlus += shift
                }
                // let siMinus = siMinus + mod(viMinus)
                siMinus += modifiers[viMinus]!!
                // let siPlus = siPlus + mod(viPlus)
                siPlus += modifiers[viPlus]!!
                // let soMinus = soMinus + mod(voMinus)
                soMinus += modifiers[voMinus]!!
                // let soPlus = soPlus + mod(voPlus)
                soPlus += modifiers[voPlus]!!
            }

            // if NextRight(viMinus) != 0 and NextRight(voPlus) = 0
            if (nextRight(viMinus) != null && nextRight(voPlus) == null) {
                // let thread(voPlus) = NextRight(viMinus)
                threads[voPlus] = nextRight(viMinus)
                // let mod(voPLus) = mod(voPlus) + siMinus - soPLus
                modifiers[voPlus] = modifiers[voPlus]!! + siMinus - soPlus
            }

            // if NextLeft(viPlus) != 0 and NextLeft(voMinus) = 0
            if (nextLeft(viPlus) != null && nextLeft(voMinus) == null) {
                // let thread(voMinus) = NextLeft(viPlus)
                threads[voMinus] = nextLeft(viPlus)
                // let mod(voMinus) = mod(voMinus) + siPlus - soMinus
                modifiers[voMinus] = modifiers[voMinus]!! + siPlus - soMinus
                // let defaultAncestor = v
                return v
            }
        }

        return null
    }

    // might be tweaked for better results
    private fun adjustedDistance(
        a: EPATreeNode<T>,
        b: EPATreeNode<T>,
    ): Float {
//        val depth = max(a.level, b.level).coerceAtLeast(1)
//        val weightA = countLeaves(a)
//        val weightB = countLeaves(b)
//        return distance * (weightA + weightB).toFloat() / 2f / depth
        return distance
    }

    private fun nextLeft(v: EPATreeNode<T>): EPATreeNode<T>? {
        // if v has a child
        return if (v.hasChildren()) {
            // return the leftmost child of v
            v.leftmostChild()
        } else { // else
            // return thread(v)
            threads[v]
        }
    }

    private fun nextRight(v: EPATreeNode<T>): EPATreeNode<T>? {
        // if v has a child
        return if (v.hasChildren()) {
            // return the rightmost child of v
            v.rightmostChild()
        } else { // else
            // return thread(v)
            threads[v]
        }
    }

    private fun moveSubtree(
        wMinus: EPATreeNode<T>,
        wPlus: EPATreeNode<T>,
        shift: Float,
    ) {
        // let subtrees = number(w+) − number(w−)
        val subtrees = wPlus.number() - wMinus.number()
        // let change(w+) = change(w+) − shift / subtrees
        changes[wPlus] = (changes[wPlus] ?: 0.0f) - (shift / subtrees.toFloat())
        // let shift(w+) = shift(w+) + shift
        shifts[wPlus] = (shifts[wPlus] ?: 0.0f) + shift
        // let change(w−) = change(w−) + shift / subtrees
        changes[wMinus] = (changes[wMinus] ?: 0.0f) + (shift / subtrees.toFloat())
        // let prelim(w+) = prelim(w+) + shift
        prelim[wPlus] = (prelim[wPlus] ?: 0.0f) + shift
        // let mod(w+) = mod(w+) + shift
        modifiers[wPlus] = modifiers[wPlus]!! + shift
    }

    private fun executeShifts(v: EPATreeNode<T>) {
        // let shift = 0
        var shift = 0.0f
        // let change = 0
        var change = 0.0f

        // for all children w of v from right to left
        v.children().reversed().forEach { w ->
            // let prelim(w) = prelim(w) + shift
            prelim[w] = prelim[w]!! + shift
            // let mod(w) = mod(w) + shift
            modifiers[w] = modifiers[w]!! + shift
            // let change = change + change(w)
            change += changes[w]!!
            // let shift = shift + shift(w) + change
            shift += shifts[w]!! + change
        }
    }

    private fun ancestor(
        viMinus: EPATreeNode<T>,
        v: EPATreeNode<T>,
        defaultAncestor: EPATreeNode<T>,
    ): EPATreeNode<T> {
        // if ancestor (viMinus) is a sibling of v
        val a = ancestor[viMinus]!!
        return if (a.parent == v.parent) {
            // return ancestor (viMinus)
            a
        } else { // else
            // return defaultAncestor
            defaultAncestor
        }
    }

    private fun secondWalk(
        v: EPATreeNode<T>,
        m: Float,
    ) {
        // let x(v) = prelim(v) + m
        val x = prelim[v]!! + m
        // let y(v) be the level of v
        val y = v.depth.toFloat() * yDistance

        xMax = max(x, xMax)
        xMin = min(x, xMin)
        maxDepth = max(maxDepth, v.depth)

        nodePlacementInformationByState[v.state] = NodePlacementInformation(Coordinate(x, y), v)

        // for all children w of v
        v.children().forEach { w ->
            // SecondWalk(w, m + mod(v))
            secondWalk(w, m + modifiers[v]!!)
        }
    }

    override fun build(tree: EPATreeNode<T>) {
        logger.info { "Building tree layout" }
        logger.info { "initializing" }
        // for all nodes v of T
        tree.forEach { v ->
            // let mod(v) = thread(v) = 0
            modifiers[v] = 0.0f
            threads[v] = null
            // let ancestor (v) = v
            ancestor[v] = v

            shifts[v] = 0.0f
            changes[v] = 0.0f
        }
        // let r be the root of T
        val r = tree

        // FirstWalk(r)
        logger.info { "first walk" }
        firstWalk(r)
        logger.info { "second walk" }
        // SecondWalk(r, −prelim(r))
        secondWalk(r, -prelim[r]!!)
        isBuilt = true
        logger.info { "finished layout construction" }
    }

    override fun getCoordinate(state: State): Coordinate = nodePlacementInformationByState[state]!!.coordinate

    override fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacementInformation<T>> {
        TODO("Not yet implemented")
    }

    override fun getMaxDepth(): Int = maxDepth

    override fun isBuilt(): Boolean = isBuilt
}
