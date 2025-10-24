package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.labels

import moritz.lindner.masterarbeit.epa.domain.State
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Surface
import org.jetbrains.skia.TextLine
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue

/** Can handle Multithreaded access */
class StateLabels(
    private val backgroundColor: Int,
    private val baseFontSize: Float,
    private val maxStateLabelLength: Int = 30
) {
    private val labelByState = ConcurrentHashMap<String, Image>()
    private val labelSizeByState = ConcurrentHashMap<State, Pair<Float, Float>>()

    private val paint =
        Paint().apply {
            color = Color.BLACK
            mode = PaintMode.FILL
            isAntiAlias = true
        }

    private val skFont =
        Font()
            .apply { size = baseFontSize }

    fun getLabelForState(state: State): Image =
        labelByState[trimStateName(state)] ?: throw IllegalStateException("Couldn't find label for state $state")

    fun generateLabelForState(state: State) {
        val label = trimStateName(state)
        if (!labelByState.containsKey(label)) {
            val textLine =
                TextLine.Companion
                    .make(label, skFont)

            val width = (textLine.width + 10f).toInt()
            val height = (textLine.ascent.absoluteValue + textLine.descent + 4f).toInt()

            val surface = Surface.Companion.makeRasterN32Premul(width, height)
            val canvas = surface.canvas

            canvas.clear(backgroundColor)

            canvas.drawTextLine(textLine, 5f, textLine.ascent.absoluteValue + 2f, paint)

            val image = surface.makeImageSnapshot()
            labelByState[label] = image
            labelSizeByState[state] = image.width.toFloat() to image.height.toFloat()
        } else {
            val image = labelByState[label]!!
            labelSizeByState[state] = image.width.toFloat() to image.height.toFloat()
        }
    }

    private fun trimStateName(state: State): String {
        return if (state.name.length > maxStateLabelLength) {
            "${state.name.take(maxStateLabelLength)}..."
        } else state.name
    }

    fun getLabelSizeMap(): Map<State, Pair<Float, Float>> {
        return labelSizeByState.toMap()
    }
}