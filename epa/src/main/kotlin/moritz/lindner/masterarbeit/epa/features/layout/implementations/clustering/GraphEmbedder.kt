package moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import org.deeplearning4j.graph.Graph
import org.deeplearning4j.graph.api.Edge
import org.deeplearning4j.graph.api.Vertex
import org.deeplearning4j.graph.models.deepwalk.DeepWalk

class GraphEmbedder(
    private val epa: ExtendedPrefixAutomaton<Long>,
    private val config: LayoutConfig.StateClusteringLayoutConfig,
    private val progressCallback: EpaProgressCallback?
) {
    fun computeEmbeddings(): Map<State, DoubleArray> {
        val (graph, stateToVertex) = buildGraph()

        val deepWalk = DeepWalk.Builder<State, Edge<Transition>>()
            .windowSize(config.windowSize)
            .vectorSize(config.graphEmbeddingDims)
            .learningRate(0.025)
            .seed(12345)
            .build()

        // Initialize with balanced degrees to avoid Huffman tree issues
        val degrees = IntArray(epa.states.size) { 1 }
        deepWalk.initialize(degrees)
        deepWalk.fit(graph, config.walkLength)

        var c = 0
        val total = epa.states.size
        return epa.states.associateWith { state ->
            progressCallback?.onProgress(c, total, "Save graph embedding")
            val vertex = stateToVertex[state]!!
            val vector = deepWalk.getVertexVector(vertex)
            DoubleArray(config.graphEmbeddingDims) { i -> vector.getDouble(i) }
        }
    }

    private fun buildGraph(): Pair<Graph<State, Edge<Transition>>, Map<State, Vertex<State>>> {
        val states = epa.states.toList()
        val vertices = mutableListOf<Vertex<State>>()
        val stateToVertex = mutableMapOf<State, Vertex<State>>()

        states.forEachIndexed { index, state ->
            val vertex = Vertex(index, state)
            vertices.add(vertex)
            stateToVertex[state] = vertex
        }

        val edges = mutableListOf<Edge<Transition>>()
        epa.transitions.forEach { transition ->
            val fromVertex = stateToVertex[transition.start]
            val toVertex = stateToVertex[transition.end]
            if (fromVertex != null && toVertex != null) {
                edges.add(Edge<Transition>(fromVertex.vertexID(), toVertex.vertexID(), transition, true))
            }
        }

        return Pair(Graph<State, Edge<Transition>>(vertices, true), stateToVertex)
    }
}