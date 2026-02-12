package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.labels

import moritz.lindner.masterarbeit.epa.domain.State
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontEdging
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
    baseFontSize: Float,
    private val maxStateLabelLength: Int = 30,
    val scale: Float
) {
    private val labelByState = ConcurrentHashMap<String, Image>()
    private val labelSizeByState = ConcurrentHashMap<State, Pair<Float, Float>>()

    private val scaledFontSize = baseFontSize * scale

    private val paint =
        Paint().apply {
            color = Color.BLACK
            mode = PaintMode.FILL
            isAntiAlias = true
        }

    private val skFont = Font().apply {
        size = scaledFontSize
        isSubpixel = true
        edging = FontEdging.SUBPIXEL_ANTI_ALIAS
    }

    fun getLabelForState(state: State): Image =
        labelByState[trimStateName(state)] ?: throw IllegalStateException("Couldn't find label for state $state")

    fun generateLabelForState(state: State) {
        val label = trimStateName(state)
        if (!labelByState.containsKey(label)) {
            val textLine =
                TextLine
                    .make(label, skFont)

            val width = (textLine.width + (10f * scale)).toInt()
            val height = (textLine.height + (4f * scale)).toInt()

            val surface = Surface.makeRasterN32Premul(width, height)
            val canvas = surface.canvas

            canvas.clear(backgroundColor)

            canvas.drawTextLine(textLine, 5f * scale, textLine.ascent.absoluteValue + (2f * scale), paint)

            val image = surface.makeImageSnapshot()
            labelByState[label] = image
            labelSizeByState[state] = (image.width / scale) to (image.height / scale)
        } else {
            val image = labelByState[label]!!
            labelSizeByState[state] = getWithAndHeight(image)
        }
    }

    private fun getWithAndHeight(image: Image, padding: Float = 8.0f): Pair<Float, Float> =
        image.width.toFloat() + padding to image.height.toFloat() + padding

    private fun trimStateName(state: State): String {
        return if (state.name.length > maxStateLabelLength) {
            "${state.name.take(maxStateLabelLength)}..."
        } else state.name
    }

    fun getLabelSizeMap(): Map<State, Pair<Float, Float>> {
        return labelSizeByState.toMap()
    }
}