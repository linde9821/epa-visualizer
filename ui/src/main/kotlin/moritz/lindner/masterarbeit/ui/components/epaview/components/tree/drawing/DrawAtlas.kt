package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode

data class TransitionAtlasEntry(
    val paint: Paint,
)

class DrawAtlas(
) {
    val selectedStatePaint =
        Paint().apply {
            color = org.jetbrains.skia.Color.BLUE
            mode = PaintMode.STROKE
            isAntiAlias = true
            strokeWidth = 5f
        }

    val stateEntryByState = HashMap<State, StateAtlasEntry>()
    val transitionEntryByParentState = HashMap<State, TransitionAtlasEntry>()

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
            val atlas = DrawAtlas()

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