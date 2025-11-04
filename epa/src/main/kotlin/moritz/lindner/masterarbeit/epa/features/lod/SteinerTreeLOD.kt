package moritz.lindner.masterarbeit.epa.features.lod

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequency
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import kotlin.math.E
import kotlin.math.exp
import kotlin.math.ln

data class SteinerLODLevel(
    val terminals: Set<State>,
    val steinerTree: Set<State>,
    val aggregationInfo: Map<State, AggregationInfo>
)

class SteinerTreeLOD<T : Comparable<T>>(
    private val lodLevels: List<SteinerLODLevel>,
    initialLevel: Float = 0f
) : LODQuery {

    // Current LOD level (continuous value)
    var lodLevel: Float = initialLevel
        private set

    private var lowerLevel: SteinerLODLevel = lodLevels[0]
    private var upperLevel: SteinerLODLevel = lodLevels[0]
    private var interpolationFactor: Float = 0f

    init {
        updateInterpolation()
    }

    /**
     * Map canvas zoom scale to LOD level
     *
     * @param scale Current canvas scale
     * @param minScale Minimum allowed scale (zoomed out)
     * @param maxScale Maximum allowed scale (zoomed in)
     */
    fun setLODFromZoom(scale: Float, minScale: Float, maxScale: Float) {
        val numLevels = lodLevels.size

        // Logarithmic mapping
        val logScale = ln(scale.coerceAtLeast(minScale))
        val logMin = ln(minScale)
        val logMax = ln(maxScale)

        val normalizedLog = ((logScale - logMin) / (logMax - logMin))
            .coerceIn(0f, 1f)

        lodLevel = (numLevels - 1) * (1f - normalizedLog)
        updateInterpolation()
    }

    private fun updateInterpolation() {
        val lowerIndex = lodLevel.toInt().coerceIn(0, lodLevels.size - 1)
        val upperIndex = (lowerIndex + 1).coerceIn(0, lodLevels.size - 1)

        lowerLevel = lodLevels[lowerIndex]
        upperLevel = lodLevels[upperIndex]
        interpolationFactor = lodLevel - lowerIndex
    }

    override fun isVisible(state: State): Boolean {
        // A state is visible if it's in either level during transition
        return state in lowerLevel.steinerTree || state in upperLevel.steinerTree
    }

    override fun isVisible(transition: Transition): Boolean {
        // Transition is visible if both endpoints are visible
        return isVisible(transition.start) && isVisible(transition.end)
    }

    override fun getOpacity(state: State): Float {
        val inLower = state in lowerLevel.steinerTree
        val inUpper = state in upperLevel.steinerTree

        return when {
            inLower && inUpper -> 1f // Always visible
            inLower && !inUpper -> 1f - smoothStep(interpolationFactor) // Fading out
            !inLower && inUpper -> smoothStep(interpolationFactor) // Fading in
            else -> 0f // Not visible
        }
    }

    /** Smooth interpolation function (ease-in-out) */
    private fun smoothStep(t: Float): Float {
        return t * t * (3f - 2f * t)
    }

    override fun getAggregationInfo(state: State): AggregationInfo? {
        TODO("Not yet implemented")
    }
}

class SteinerTreeLODBuilder<T : Comparable<T>>(private val epa: ExtendedPrefixAutomaton<T>) {

    private val epaService = EpaService<T>()
    private val pathByRoute = mutableMapOf<Pair<State, State>, List<State>>()

    /** Build LOD levels using Steiner tree approximation */
    fun buildLODLevels(
        numLevels: Int = 3,
        minThreshold: Float = 0.0f
    ): List<SteinerLODLevel> {
        require(numLevels > 0) { "numLevels must be positive" }

        val normalizedFrequency = epaService.getNormalizedStateFrequency(epa)

        // Generate exponentially increasing thresholds
        val thresholds = quantileThresholds(normalizedFrequency.toList(), numLevels)

        return thresholds.map { threshold ->
            val terminals = selectTerminals(threshold, normalizedFrequency)
            buildSteinerLevel(terminals, normalizedFrequency)
        }
    }

    fun quantileThresholds(
        data: List<Float>,
        numLevels: Int
    ): List<Float> {
        require(numLevels >= 2) { "numLevels must be at least 2" }
        if (data.isEmpty()) return emptyList()

        val sorted = data.sorted()
        val size = sorted.size - 1

        return (0 until numLevels).map { i ->
            val p = i.toFloat() / (numLevels - 1)
            val index = (p * size).toInt().coerceIn(0, size)
            sorted[index]
        }
    }

    /** Build a single Steiner LOD level using JGraphT */
    private fun buildSteinerLevel(
        terminals: Set<State>,
        normalizedFrequency: NormalizedStateFrequency,
    ): SteinerLODLevel {
        if (terminals.size <= 1) {
            return SteinerLODLevel(
                terminals = terminals,
                steinerTree = terminals,
                aggregationInfo = emptyMap()
            )
        }

        val steinerStates = mutableSetOf<State>()

        val terminalsList = terminals.toList().crossProduct()

        terminalsList.forEachIndexed { index, (source, target) ->
            val precomputed = pathByRoute[source to target] ?: pathByRoute[target to source]
            if (precomputed != null) {
                steinerStates.addAll(precomputed)
            } else {
                val startPath = epaService.getPathToRoot(source)
                val targetPath = epaService.getPathFromRoot(target)
                val targetPathSet = targetPath.toSet()

                val path = mutableListOf<State>()
                var matching: State? = null

                for (stateOnPath in startPath) {
                    path.add(stateOnPath)
                    if (targetPathSet.contains(stateOnPath)) {
                        matching = stateOnPath
                        break
                    }
                }

                val pathDown = targetPath.dropWhile { state -> state != matching }
                path.addAll(pathDown)

                steinerStates.addAll(path)
                pathByRoute[source to target] = path
            }
        }

        // Step 3: Compute aggregation info
        val aggregationInfo = computeAggregationInfo(steinerStates, normalizedFrequency)

        return SteinerLODLevel(
            terminals = terminals,
            steinerTree = steinerStates,
            aggregationInfo = aggregationInfo
        )
    }

    /** Build an undirected JGraphT graph from the EPA */
    private fun buildUndirectedGraph(): Graph<State, DefaultEdge> {
        val graph = SimpleGraph<State, DefaultEdge>(DefaultEdge::class.java)

        // Add all states as vertices
        for (state in epa.states) {
            graph.addVertex(state)
        }

        // Add all transitions as undirected edges
        for (transition in epa.transitions) {
            graph.addEdge(transition.start, transition.end)
        }

        return graph
    }

    /** Compute aggregation information for the Steiner tree */
    private fun computeAggregationInfo(
        steinerStates: Set<State>,
        normalizedFrequency: NormalizedStateFrequency
    ): Map<State, AggregationInfo> {
        val aggregationInfo = mutableMapOf<State, AggregationInfo>()

        for (state in steinerStates) {
            val info = computeHiddenSubtree(state, steinerStates, normalizedFrequency)
            if (info.hiddenChildCount > 0) {
                aggregationInfo[state] = info
            }
        }

        return aggregationInfo
    }

    /** Compute what's hidden in the subtree below this state */
    private fun computeHiddenSubtree(
        state: State,
        visibleStates: Set<State>,
        normalizedFrequency: NormalizedStateFrequency
    ): AggregationInfo {
        var hiddenCount = 0
        val hiddenPartitions = mutableSetOf<Int>()
        var totalEventCount = 0

        fun traverse(current: State) {
            val outgoing = epa.outgoingTransitionsByState[current] ?: emptyList()

            for (transition in outgoing) {
                val child = transition.end

                if (child !in visibleStates) {
                    hiddenCount++

                    // Get partition
                    val partition = epa.partition(child)
                    hiddenPartitions.add(partition)

                    // Approximate event count
                    val freq = normalizedFrequency.frequencyByState(child)
                    totalEventCount += (freq * 1000).toInt()

                    // Recursively traverse hidden children
                    traverse(child)
                }
            }
        }

        traverse(state)

        return AggregationInfo(
            hiddenChildCount = hiddenCount,
            hiddenPartitions = hiddenPartitions,
            totalEventCount = totalEventCount
        )
    }

    /**
     * Select terminal states based on frequency threshold
     *
     * Terminals are states that MUST be included in the LOD level:
     * 1. Root state (always)
     * 2. States with frequency >= threshold
     */
    private fun selectTerminals(threshold: Float, normalizedFrequency: NormalizedStateFrequency): Set<State> {
        val terminals = mutableSetOf<State>()

        // Always include root
        terminals.add(State.Root)

        // Include high-frequency states
        for (state in epa.states) {
            val freq = normalizedFrequency.frequencyByState(state)
            if (freq >= threshold) {
                terminals.add(state)
            }
        }

        return terminals
    }

    private fun <T> List<T>.crossProduct(): List<Pair<T, T>> {
        return this.flatMap { a ->
            this.map { b ->
                a to b
            }
        }
    }
}