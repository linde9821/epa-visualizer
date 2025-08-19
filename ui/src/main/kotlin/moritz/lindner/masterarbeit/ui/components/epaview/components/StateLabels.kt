package moritz.lindner.masterarbeit.ui.components.epaview.components

import moritz.lindner.masterarbeit.epa.domain.State
import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import kotlin.math.absoluteValue

class StateLabels(
    val backgroundColor: Int,
    val baseFontSize: Float,
) {
    private val labelByState = HashMap<String, Image>()

    val paint =
        org.jetbrains.skia.Paint().apply {
            color = Color.BLACK
            mode = org.jetbrains.skia.PaintMode.FILL
            isAntiAlias = true
        }

    val skFont =
        org.jetbrains.skia
            .Font()
            .apply { size = baseFontSize }

    fun getLabelForState(state: State): Image? = labelByState[state.name]

    fun generateLabelForState(state: State) {
        val label = state.name
        if (!labelByState.containsKey(label)) {
            val textLine =
                org.jetbrains.skia.TextLine
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
