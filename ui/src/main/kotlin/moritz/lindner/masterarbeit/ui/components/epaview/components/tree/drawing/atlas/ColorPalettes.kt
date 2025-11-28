package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas

import kotlinx.serialization.json.Json
import org.jetbrains.skia.Color

object ColorPalettes {

    fun colorPalette(name: String): IntArray {
        return heatmapByName[name]!!
    }

    // inspired seaborn https://seaborn.pydata.org/tutorial/color_palettes.html
    // and taken from seaborn by using seaborn_color_extractor.py to generate 64-samples of each color palette
    private val heatmapByName: Map<String, IntArray> = buildMap {

        val json = Json { ignoreUnknownKeys = true }

        val jsonText = this::class.java.classLoader
            .getResourceAsStream("colormaps.json")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException("colormaps.json not found in resources")

        val data: Map<String, List<List<Int>>> = json.decodeFromString(jsonText)

        val colors = data.mapValues { (_, colors) ->
            colors.map { rgb -> Color.makeRGB(rgb[0], rgb[1], rgb[2]) }.toIntArray()
        }

        putAll(colors)
    }

}