package moritz.lindner.masterarbeit.epa.features.lod.steiner

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.lod.LODQuery
import kotlin.math.ln
import kotlin.math.roundToInt

class SteinerTreeLOD<T : Comparable<T>>(
    private val lodLevels: List<SteinerLODLevel>,
    private val minScale: Float,
    private val maxScale: Float,
) : LODQuery {

    private var level: SteinerLODLevel = lodLevels[0]
    /**
     * Map canvas zoom scale to LOD level (Logarithmic mapping)
     *
     * @param scale Current canvas scale
     * @param minScale Minimum allowed scale (zoomed out)
     * @param maxScale Maximum allowed scale (zoomed in)
     */
    fun setLODFromZoom(scale: Float) {
        val logScale = ln(scale.coerceAtLeast(minScale))
        val logMin = ln(minScale)
        val logMax = ln(maxScale)

        val normalizedLog = ((logScale - logMin) / (logMax - logMin))
            .coerceIn(0f, 1f)
        val index = (1f - normalizedLog).roundToInt().coerceIn(0, lodLevels.size - 1)
        level = lodLevels[index]
    }

    override fun isVisible(state: State): Boolean {
        return state in level.steinerTreeNodes
    }
}

