package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing

import moritz.lindner.masterarbeit.epa.domain.State
import org.jetbrains.skia.Paint

interface StateAtlasConfig {
    fun toPaint(state: State): Paint
    fun toSize(state: State): Float
}