package moritz.lindner.masterarbeit.epa.features.layout.implementations

import com.github.davidmoten.rtree2.Entry
import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import moritz.lindner.masterarbeit.epa.features.layout.placement.Vector2D
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

class PRTLayout(
    private val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val config: LayoutConfig.PRTLayoutConfig,
    backgroundDispatcher: ExecutorCoroutineDispatcher,

    ) : Layout {
    private val labelSizeByState: Map<State, Pair<Float, Float>> = config.labelSizeByState
    private val scope = CoroutineScope(backgroundDispatcher + SupervisorJob())
    private val epaService = EpaService<Long>()
    private var isBuilt = false

    private val logger = KotlinLogging.logger { }
    private val coordinateByState = mutableMapOf<State, NodePlacement>()

    private lateinit var rTree: RTree<NodePlacement, PointFloat>

    private val collisionRadiusByState: Map<State, Float> = buildMap {
        extendedPrefixAutomaton.states.forEach { state ->
            put(state, getCollisionRadius(state))
        }
    }

    private val maxCollisionRadius = collisionRadiusByState.values.max()

    private fun getCollisionRadius(state: State): Float {
        val (width, height) = labelSizeByState[state]!!

        // Use half the diagonal as radius (conservative)
        return sqrt(width * width + height * height).toFloat() / 2f

        // Or simpler: half the maximum dimension + pedding
//        return (max(width, height) / 2f) + 10f
    }

    override fun build(progressCallback: EpaProgressCallback?) {
        logger.info { "init" }
        val x = when (config.initializer) {
            LayoutConfig.PRTInitialLayout.Compact -> {
                compactInitialization(
                    extendedPrefixAutomaton = extendedPrefixAutomaton,
                    subtreeSizeByState = epaService.subtreeSizeByState(extendedPrefixAutomaton),
                    hopsFromRootByState = epaService.hopsFromRootByState(extendedPrefixAutomaton),
                    progressCallback = progressCallback
                )
            }

            LayoutConfig.PRTInitialLayout.EdgeLength -> {
                val cycleTimes = epaService.computeAllCycleTimes(
                    extendedPrefixAutomaton = extendedPrefixAutomaton,
                    minus = Long::minus,
                    average = { cycleTimes ->
                        if (cycleTimes.isEmpty()) {
                            0f
                        } else cycleTimes.average().toFloat()
                    },
                )

                // Add offset to handle zero values with logarithmic scaling
                val offset = 1.0f
                val values = cycleTimes.values.map { it + offset }
                val min = values.minOrNull() ?: offset
                val max = values.maxOrNull() ?: offset

                // Define your desired edge length range (adjust these values!)
                val minEdgeLength = 10.0f  // minimum edge length in pixels/units
                val maxEdgeLength = 1000.0f // maximum edge length in pixels/units

                val desiredEdgeLengthByTransition = if ((max - min) < 0.0001f) {
                    // All values are essentially the same - use middle of range
                    extendedPrefixAutomaton.transitions.associateWith { (minEdgeLength + maxEdgeLength) / 2 }
                } else {
                    val logMin = log10(min)
                    val logMax = log10(max)

                    extendedPrefixAutomaton.transitions.associateWith { transition ->
                        val rawValue = cycleTimes[transition.start] ?: 0.0f
                        val value = rawValue + offset
                        val logValue = log10(value)
                        val normalized = ((logValue - logMin) / (logMax - logMin)).coerceIn(0.0f, 1.0f)

                        // Map to actual edge length range
                        minEdgeLength + normalized * (maxEdgeLength - minEdgeLength)
                    }
                }

                edgeLengthInitialization(
                    extendedPrefixAutomaton = extendedPrefixAutomaton,
                    subtreeSizeByState = epaService.subtreeSizeByState(extendedPrefixAutomaton),
                    desiredEdgeLengthByTransition = desiredEdgeLengthByTransition,
                    progressCallback = progressCallback
                )
            }
        }

        logger.info { "parallelForceDirectedImprovements" }
        parallelForceDirectedImprovements(
            extendedPrefixAutomaton = extendedPrefixAutomaton,
            x = x,
            progressCallback = progressCallback
        ).forEach { (state, coordinate) ->
            coordinateByState[state] = NodePlacement(
                coordinate = coordinate,
                state = state,
            )
        }

        require(coordinateByState.size == extendedPrefixAutomaton.states.size) {
            logger.info { "Expected Size: ${extendedPrefixAutomaton.states.size} but actual was ${coordinateByState.size}" }
        }

        logger.info { "rtree" }
        rTree = RTreeBuilder.build(coordinateByState.values.toList())

        isBuilt = true
    }

    override fun isBuilt(): Boolean {
        return isBuilt
    }

    override fun getCoordinate(state: State): Coordinate {
        return coordinateByState[state]?.coordinate ?: throw IllegalStateException("No coordinate for $state")
    }

    override fun getCoordinatesInRectangle(rectangle: Rectangle): List<NodePlacement> {
        return rTree
            .search(
                Geometries.rectangle(
                    rectangle.topLeft.x,
                    rectangle.topLeft.y,
                    rectangle.bottomRight.x,
                    rectangle.bottomRight.y,
                ),
            ).map(Entry<NodePlacement, PointFloat>::value)
    }

    override fun iterator(): Iterator<NodePlacement> {
        return coordinateByState.values.iterator()
    }

    data class Wedge(
        val center: Coordinate,
        val radius: Float,
        val angleRange: Pair<Float, Float>,
    ) {
        init {
//            require(
//                angleRange.first >= 0f && angleRange.second <= (2f * PI).toFloat()
//            )
        }

        fun arcMidpoint(): Coordinate {
            val (startAngle, endAngle) = angleRange
            val midAngle = (startAngle + endAngle) / 2.0

            return Coordinate(
                x = (center.x + radius * cos(midAngle)).toFloat(),
                y = (center.y + radius * sin(midAngle)).toFloat()
            )
        }
    }

    private fun edgeLengthInitialization(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        subtreeSizeByState: Map<State, Int>,
        desiredEdgeLengthByTransition: Map<Transition, Float>,
        progressCallback: EpaProgressCallback?
    ): Map<State, Coordinate> {
        val coordinateByState = HashMap<State, Coordinate>()
        val wedgeByState = HashMap<State, Wedge>()

        val transitionsByRoot = extendedPrefixAutomaton.outgoingTransitionsByState[State.Root]!!

        val maxLength = transitionsByRoot.maxOf { transitions ->
            desiredEdgeLengthByTransition[transitions]!!
        }

        coordinateByState[State.Root] = Coordinate(0f, 0f)
        val rootWedge = Wedge(
            center = Coordinate(0f, 0f),
            radius = maxLength,
            angleRange = 0.0f to (2f * PI).toFloat()
        )
        wedgeByState[State.Root] = rootWedge

        extendedPrefixAutomaton.acceptBreadthFirst(object : AutomatonVisitor<Long> {

            override fun onProgress(current: Long, total: Long) {
                progressCallback?.onProgress(current, total, "Edge Length Initialization")
            }

            override fun visit(
                extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
                state: State,
                depth: Int
            ) {
                val parent = state

                val parentWedge = wedgeByState[parent] ?: return
                val transitions = extendedPrefixAutomaton.outgoingTransitionsByState[parent]
                    ?: return

                if (transitions.isEmpty()) return

                val subtreeSizes = transitions.map {
                    subtreeSizeByState[it.end] ?: 1
                }

                val angleRanges = partitionAngleRange(
                    parentWedge.angleRange,
                    subtreeSizes
                )

                // Place each child
                for ((transition, angleRange) in transitions.zip(angleRanges)) {
                    val child = transition.end
                    val edgeLength = desiredEdgeLengthByTransition[transition]!!

                    val childWedge = Wedge(
                        center = coordinateByState[parent]!!,
                        radius = edgeLength,
                        angleRange = angleRange
                    )

                    wedgeByState[child] = childWedge
                    coordinateByState[child] = childWedge.arcMidpoint()
                }
            }
        }
        )
        return coordinateByState
    }

    private fun partitionAngleRange(
        angleRange: Pair<Float, Float>,
        subtreeSizes: List<Int>
    ): List<Pair<Float, Float>> {
        val (start, end) = angleRange
        val totalSize = subtreeSizes.sum().toFloat()
        val totalAngle = end - start

        val ranges = mutableListOf<Pair<Float, Float>>()
        var currentAngle = start

        for (size in subtreeSizes) {
            val proportion = size / totalSize
            val childAngleSpan = proportion * totalAngle
            val endAngle = currentAngle + childAngleSpan

            ranges.add(currentAngle to endAngle)
            currentAngle = endAngle
        }

        return ranges
    }

    private fun compactInitialization(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        subtreeSizeByState: Map<State, Int>,
        hopsFromRootByState: Map<State, Int>,
        progressCallback: EpaProgressCallback?
    ): Map<State, Coordinate> {
        val coordinateByState = HashMap<State, Coordinate>()
        val wedgeByState = HashMap<State, Wedge>()

        val rootCoordinate = Coordinate(0f, 0f)
        coordinateByState[State.Root] = rootCoordinate
        wedgeByState[State.Root] = Wedge(
            center = rootCoordinate,
            radius = 1f,
            angleRange = 0.0f to (2f * PI).toFloat()
        )

        extendedPrefixAutomaton.acceptBreadthFirst(object : AutomatonVisitor<Long> {
            override fun visit(
                extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
                state: State,
                depth: Int
            ) {
                val parent = state
                val children = extendedPrefixAutomaton.outgoingTransitionsByState[state]?.map { it.end } ?: return

                if (children.isEmpty()) return

                val centralizedChildren = centralize(children)

                val subtreeSizes = centralizedChildren.map { child ->
                    subtreeSizeByState[child] ?: 1
                }

                val parentWedge = wedgeByState[parent] ?: return

                val angleRanges = partitionAngleRange(
                    parentWedge.angleRange,
                    subtreeSizes
                )

                centralizedChildren.zip(angleRanges).forEach { (child, angleRange) ->
                    val wedge = Wedge(
                        center = rootCoordinate,
                        radius = hopsFromRootByState[child]!!.toFloat() * 200,
                        angleRange = angleRange
                    )
                    wedgeByState[child] = wedge
                    coordinateByState[child] = wedge.arcMidpoint()
                }
            }

            override fun onProgress(current: Long, total: Long) {
                progressCallback?.onProgress(current, total, "Edge Length Initialization")
            }

            private fun centralize(children: List<State>): List<State> {
                val sorted = children.sortedBy { subtreeSizeByState[it]!! }
                val first = sorted.filterIndexed { index, _ -> index % 2 == 0 }
                val second = sorted.filterIndexed { index, _ -> index % 2 == 1 }

                return first.reversed() + second
            }
        })

        return coordinateByState
    }

    private fun parallelForceDirectedImprovements(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        x: Map<State, Coordinate>,
        batch: Int = 128,
        samples: Int = 20,
        iterations: Int = 50,
        progressCallback: EpaProgressCallback?
    ): Map<State, Coordinate> {
        val positions = x.toMutableMap()
        repeat(iterations) { currentIteration ->
            progressCallback?.onProgress(currentIteration, iterations, "Force directed improvements")
            logger.info { "parallelForceDirectedImprovements iteration $currentIteration" }
            val t = ConcurrentHashMap<State, Vector2D>()
            positions.keys.forEach { t[it] = Vector2D.zero() }

            val batches = extendedPrefixAutomaton.states.chunked(batch)

            val rTree = RTreeBuilder.build(positions.map {
                NodePlacement(it.value, it.key)
            })

            batches.forEach { batch ->
                runBlocking {
                    // for each node in parallel do
                    batch.map { u ->
                        yield()
                        scope.async {
                            collisionRegion(u, positions, rTree).forEach { v ->
                                val labelForce = computeLabelOverlapForce(u, v, positions)
                                t.merge(v, labelForce) { existing, new -> existing.add(new) }
                            }
                        }
                    }.awaitAll()
                }
            }

            for (state in positions.keys) {
                val movement = t[state]!!


                // Only apply if movement is non-zero
                if (movement.magnitude() > 0.0001) {
                    // Check if this movement would introduce edge crossings
                    if (!introducesEdgeCrossing(state, movement, positions)) {
                        // Apply movement
                        val currentPos = positions[state]!!
                        positions[state] = Coordinate(
                            x = (currentPos.x + movement.x),
                            y = (currentPos.y + movement.y)
                        )
                    }
                }
            }


        }

        return positions
    }

    private fun computeLabelOverlapForce(
        u: State,
        v: State,
        positions: Map<State, Coordinate>,
        strength: Float = 1.0f
    ): Vector2D {
        val posU = positions[u]!!
        val posV = positions[v]!!

        val radiusU = collisionRadiusByState[u]!!
        val radiusV = collisionRadiusByState[v]!!

        // Combined collision radius for two circles
        val collisionRadius = (radiusU + radiusV)

        // Vector from u to v (direction of repulsion)
        val dx = (posV.x - posU.x)
        val dy = (posV.y - posU.y)

        // STEP 1: Stretch y-coordinate by factor b=3
        val stretchedDy = dy * 3.0f

        // STEP 2: Compute distance in stretched space (circular collision)
        val distance = sqrt(dx * dx + stretchedDy * stretchedDy)

        return if (distance < collisionRadius && distance > 0.0) {
            // STEP 2 (continued): Circular repulsive force in stretched space
            // Force magnitude inversely proportional to distance
            val forceMagnitude = ((collisionRadius - distance) / distance) * strength

            // Force components in stretched space
            val forceX = dx * forceMagnitude
            val forceY = stretchedDy * forceMagnitude

            // STEP 3: Un-stretch the force with reciprocal scaling
            Vector2D(
                x = forceX,
                y = forceY / 3.0f       // y scaled by 1/b (reciprocal)
            )
        } else Vector2D.zero()
    }

    private fun collisionRegion(
        state: State,
        positions: Map<State, Coordinate>,
        rTree: RTree<NodePlacement, PointFloat>,
    ): List<State> {
        val statePosition = positions[state]!!
        val stateRadius = collisionRadiusByState[state]!!

        val searchRadius = (stateRadius + maxCollisionRadius)

        return rTree
            .search(
                Geometries.rectangle(
                    statePosition.x - searchRadius,
                    statePosition.y - searchRadius,
                    statePosition.x + searchRadius,
                    statePosition.y + searchRadius
                )
            )
            .map { entry -> entry.value().state }
            .filter { it != state }
    }

    private fun introducesEdgeCrossing(
        node: State,
        movement: Vector2D,
        positions: Map<State, Coordinate>
    ): Boolean {
        val currentPos = positions[node]!!
        val newPos = Coordinate(
            x = (currentPos.x + movement.x),
            y = (currentPos.y + movement.y)
        )

        // Get all edges connected to this node (edges that will move)
        val outGoingEdges = extendedPrefixAutomaton.outgoingTransitionsByState[node] ?: emptyList()
        val incomingEdge = extendedPrefixAutomaton.incomingTransitionsByState[node] ?: emptyList()
        val affectedEdges = outGoingEdges + incomingEdge

        if (affectedEdges.isEmpty()) return false

        affectedEdges.forEach { affectedEdge ->
            val edgeStart = if (affectedEdge.start == node) {
                newPos
            } else {
                positions[affectedEdge.start]!!
            }

            val edgeEnd = if (affectedEdge.end == node) {
                newPos
            } else {
                positions[affectedEdge.end]!!
            }

            // Check against all other edges
            for (otherEdge in extendedPrefixAutomaton.transitions) {
                // Skip if same edge or if edges share a node
                if (affectedEdge == otherEdge || sharesNode(affectedEdge, otherEdge)) {
                    continue
                }

                val otherStart = positions[otherEdge.start]!!
                val otherEnd = positions[otherEdge.end]!!

                // Check if segments intersect
                if (segmentsIntersect(edgeStart, edgeEnd, otherStart, otherEnd)) {
                    return true  // Crossing detected!
                }
            }
        }

        return false
    }

    private fun sharesNode(edge1: Transition, edge2: Transition): Boolean {
        return edge1.start == edge2.start ||
                edge1.start == edge2.end ||
                edge1.end == edge2.start ||
                edge1.end == edge2.end
    }

    private fun segmentsIntersect(
        p1: Coordinate, p2: Coordinate,  // First segment
        p3: Coordinate, p4: Coordinate   // Second segment
    ): Boolean {
        // Using the orientation method
        val o1 = orientation(p1, p2, p3)
        val o2 = orientation(p1, p2, p4)
        val o3 = orientation(p3, p4, p1)
        val o4 = orientation(p3, p4, p2)

        // General case: segments intersect if orientations differ
        if (o1 != o2 && o3 != o4) {
            return true
        }

        // Special cases: collinear points (usually can ignore for trees)
        return false
    }

    private fun orientation(p: Coordinate, q: Coordinate, r: Coordinate): Int {
        val value = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)

        return when {
            abs(value) < 1e-6f -> 0  // Collinear
            value > 0 -> 1           // Clockwise
            else -> 2                // Counterclockwise
        }
    }

}