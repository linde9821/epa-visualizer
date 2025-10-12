package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode

data class TransitionAtlasEntry(
    val startWidth: Float,
    val endWith: Float,
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

    val atlasByState = HashMap<State, StateAtlasEntry>()
//    val transitionByState = HashMap<State, TransitionAtlasEntry>()

    fun add(state: State, entry: StateAtlasEntry) {
        atlasByState[state] = entry
    }

    fun get(state: State): StateAtlasEntry {
        return atlasByState[state] ?: throw IllegalStateException("For $state no entry in the draw atlas is present")
    }

    companion object Companion {
        fun <T : Comparable<T>> build(
            extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
            stateAtlasConfig: StateAtlasConfig
        ): DrawAtlas {
            val atlas = DrawAtlas()

            extendedPrefixAutomaton.states.forEach { state ->
                val paint = stateAtlasConfig.toPaint(state)
                val size = stateAtlasConfig.toSize(state)
                atlas.add(state, StateAtlasEntry(size, paint))
            }

            return atlas
        }
    }
}