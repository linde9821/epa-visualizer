package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import moritz.lindner.masterarbeit.epa.features.layout.ColorPalettes

@Composable
fun CreatePaletteBrush(paletteName: String): Brush {
    val packedColors = remember(paletteName) {
        ColorPalettes.colorPalette(paletteName)
    }

    val composeColors = remember(packedColors) {
        packedColors.map { packed ->
            val r = (packed shr 16) and 0xFF
            val g = (packed shr 8) and 0xFF
            val b = packed and 0xFF
            Color(
                red = r,
                green = g,
                blue = b
            )
        }
    }

    return Brush.horizontalGradient(composeColors)
}