package moritz.lindner.masterarbeit.epa.features.layout

import kotlinx.serialization.json.Json
import java.io.BufferedReader

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
            ?.use(BufferedReader::readText)
            ?: throw IllegalStateException("colormaps.json not found in resources")

        if (jsonText.startsWith("version https://git-lfs")) {
            throw IllegalStateException(
                "Detected Git LFS pointer instead of actual file. " +
                        "Ensure 'git lfs pull' was run and LFS is correctly configured in CI."
            )
        }

        val data = json
            .decodeFromString<Map<String, List<List<Int>>>>(jsonText) + Pair(
            "Black",
            listOf(listOf(0, 0, 0), listOf(0, 0, 0), listOf(0, 0, 0))
        )

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