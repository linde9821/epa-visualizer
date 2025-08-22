package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import moritz.lindner.masterarbeit.epa.domain.State
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.TextLine
import kotlin.math.absoluteValue

class StateLabels(
    private val backgroundColor: Int,
    private val baseFontSize: Float,
) {
    private val labelByState = HashMap<String, Image>()

    private val paint =
        Paint().apply {
            color = Color.BLACK
            mode = PaintMode.FILL
            isAntiAlias = true
        }

    private val skFont =
        Font()
            .apply { size = baseFontSize }

    fun getLabelForState(state: State): Image? = labelByState[state.name]

    fun generateLabelForState(state: State) {
        val label = state.name
        if (!labelByState.containsKey(label)) {
            val textLine =
                TextLine
                    .make(label, skFont)

            val width = (textLine.width + 10f).toInt()
            val height = (textLine.ascent.absoluteValue + textLine.descent + 4f).toInt()

            val surface = Surface.makeRasterN32Premul(width, height)
            val canvas = surface.canvas

            canvas.clear(backgroundColor)

            canvas.drawTextLine(textLine, 5f, textLine.ascent.absoluteValue + 2f, paint)

            val image = surface.makeImageSnapshot()
            labelByState[label] = image
        }
    }
}
