package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.PRTLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.PartitionClusteringLayout
import moritz.lindner.masterarbeit.epa.features.layout.implementations.clustering.StateClusteringLayout
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode

enum class TransitionDrawMode {
    NONE,
    BEZIER,
    LINE;
}

class DrawAtlas(
    val stateSizeUntilLabelIsDrawn: Float
) {

    val selectedStatePaint =
        Paint().apply {
            color = Color.BLUE
            mode = PaintMode.STROKE
            isAntiAlias = true
            strokeWidth = 5f
        }

    val highlightedPaint = Paint().apply {
        color = Color.CYAN
        mode = PaintMode.STROKE
        isAntiAlias = true
        strokeWidth = 5f
    }

    val tokenPaint = Paint().apply {
        color = Color.MAGENTA
        mode = PaintMode.FILL
        isAntiAlias = true
        strokeWidth = 5f
    }

    val stateEntryByState = HashMap<State, StateAtlasEntry>()
    val transitionEntryByParentState = HashMap<State, TransitionAtlasEntry>()

    fun getTransitionModeForLayout(layout: Layout): TransitionDrawMode {
        return when (layout) {
            is StateClusteringLayout, is PartitionClusteringLayout -> TransitionDrawMode.NONE
            is PRTLayout -> TransitionDrawMode.LINE
            else -> TransitionDrawMode.BEZIER
        }
    }

    fun add(state: State, entry: StateAtlasEntry) {
        stateEntryByState[state] = entry
    }

    fun add(transition: Transition, entry: TransitionAtlasEntry) {
        transitionEntryByParentState[transition.end] = entry
    }

    fun getState(state: State): StateAtlasEntry {
        return stateEntryByState[state]
            ?: throw IllegalStateException("For $state no entry in the draw atlas is present")
    }

    fun getTransitionEntryByParentState(parentState: State): TransitionAtlasEntry {
        return transitionEntryByParentState[parentState]
            ?: throw IllegalStateException("For $parentState no entry in the draw atlas is present")
    }

    companion object Companion {
        fun <T : Comparable<T>> build(
            extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
            atlasConfig: AtlasConfig,
            progressCallback: EpaProgressCallback? = null
        ): DrawAtlas {
            val atlas = DrawAtlas(13f)

            extendedPrefixAutomaton.states.forEachIndexed { index, state ->
                progressCallback?.onProgress(
                    index,
                    extendedPrefixAutomaton.states.size,
                    "Building draw atlas for states"
                )
                atlas.add(state, atlasConfig.toStateAtlasEntry(state))
            }

            extendedPrefixAutomaton.transitions.forEachIndexed { index, transition ->
                progressCallback?.onProgress(
                    index,
                    extendedPrefixAutomaton.transitions.size,
                    "Building draw atlas for transitions"
                )
                atlas.add(transition, atlasConfig.toTransitionAtlasEntry(transition))
            }

            return atlas
        }
    }
}