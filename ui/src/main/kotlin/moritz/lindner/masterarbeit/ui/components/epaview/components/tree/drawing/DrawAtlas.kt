package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode

data class TransitionAtlasEntry(
    val paint: Paint,
)

class DrawAtlas(
) {
    val baseColor = Paint().apply {
        color = Color.BLACK
        mode = PaintMode.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    val entryByState = HashMap<State, StateAtlasEntry>()
    val entryByTransition = HashMap<Transition, TransitionAtlasEntry>()

    val entryByParent = HashMap<State, TransitionAtlasEntry>()

    fun add(state: State, entry: StateAtlasEntry) {
        entryByState[state] = entry
    }

    fun add(transition: Transition, entry: TransitionAtlasEntry) {
        entryByTransition[transition] = entry
        entryByParent[transition.start] = entry
    }

    fun get(state: State): StateAtlasEntry {
        return entryByState[state] ?: throw IllegalStateException("For $state no entry in the draw atlas is present")
    }

    fun get(transition: Transition): TransitionAtlasEntry {
        return entryByTransition[transition] ?: throw IllegalStateException("For $transition no entry in the draw atlas is present")
    }

    fun getTransitionByParentState(parentState: State): TransitionAtlasEntry {
        return entryByParent[parentState] ?: throw IllegalStateException("For $parentState no entry in the draw atlas is present")
    }


    companion object Companion {
        fun <T : Comparable<T>> build(
            extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
            stateAtlasConfig: StateAtlasConfig
        ): DrawAtlas {
            val atlas = DrawAtlas()

            extendedPrefixAutomaton.states.forEach { state ->
                atlas.add(state, stateAtlasConfig.toStateAtlasEntry(state))
            }

            extendedPrefixAutomaton.transitions.forEach { transition ->
                atlas.add(transition, stateAtlasConfig.toTransitionAtlasEntry(transition))
            }

            return atlas
        }
    }
}