package moritz.lindner.masterarbeit.epa.features.layout

import kotlinx.serialization.json.Json

object ColorPalettes {

    fun colorPalette(name: String): IntArray {
        return heatmapByName[name]!!
    }

    fun allPalettes() = heatmapByName.keys.sorted().toList()

    private val heatmapByName: Map<String, IntArray> = buildMap {
        val json = Json { ignoreUnknownKeys = true }

        val jsonText = this::class.java.classLoader
            .getResourceAsStream("colormaps.json")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException("colormaps.json not found in resources")

        val data: Map<String, List<List<Int>>> = json.decodeFromString(jsonText)

        val colors = data.mapValues { (_, colors) ->
            // Pack RGB into a single Int: 0xRRGGBB
            colors.map { rgb ->
                packRgb(rgb)
            }.toIntArray()
        }

        putAll(colors)
    }

    private fun packRgb(rgb: List<Int>): Int = (rgb[0] shl 16) or (rgb[1] shl 8) or rgb[2]
}