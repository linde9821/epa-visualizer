package moritz.lindner.masterarbeit.epa.features.layout.implementations

import com.github.davidmoten.rtree2.Entry
import com.github.davidmoten.rtree2.RTree
import com.github.davidmoten.rtree2.geometry.Geometries
import com.github.davidmoten.rtree2.geometry.internal.PointFloat
import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.placement.Coordinate
import moritz.lindner.masterarbeit.epa.features.layout.placement.NodePlacement
import moritz.lindner.masterarbeit.epa.features.layout.placement.Rectangle
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin

class PRTLayout(
    private val extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
) : Layout {
    private val epaService = EpaService<Long>()
    private var isBuilt = false

    private val logger = KotlinLogging.logger { }
    private val coordinateByState = mutableMapOf<State, NodePlacement>()

    private lateinit var rTree: RTree<NodePlacement, PointFloat>

    override fun build(progressCallback: EpaProgressCallback?) {

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
            desiredEdgeLengthByTransition = desiredEdgeLengthByTransition
        ).forEach { state, coordinate ->
            coordinateByState[state] = NodePlacement(
                coordinate = coordinate,
                state = state,
            )
        }

        coordinateByState.forEach {
            logger.info { "State: ${it.key} Coordinate: ${it.value}" }
        }

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
        desiredEdgeLengthByTransition: Map<Transition, Float>
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
}