package moritz.lindner.masterarbeit.epa.features.lod.steiner

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.lod.LODQuery

class SteinerTreeLOD<T : Comparable<T>>(
    private val lodLevels: List<SteinerLODLevel>,
    private val minScale: Float,
    private val maxScale: Float,
    initialLevel: Float = 0f
) : LODQuery {

    private val logger = KotlinLogging.logger { }

    // Current LOD level (continuous value)
    var lodLevel: Float = initialLevel
        private set

    private var lowerLevel: SteinerLODLevel = lodLevels[0]
    private var upperLevel: SteinerLODLevel = lodLevels[0]
    private var interpolationFactor: Float = 0f

    init {
        updateInterpolation()
    }

    /**
     * Map canvas zoom scale to LOD level (Logarithmic mapping)
     *
     * @param scale Current canvas scale
     * @param minScale Minimum allowed scale (zoomed out)
     * @param maxScale Maximum allowed scale (zoomed in)
     */
    fun setLODFromZoom(scale: Float) {
        lodLevel = if (scale < 0.5f) 1f else 0f
//        lodLevel = (numLevels - 1) * (1f - normalizedLog)
        logger.info { "For scale $scale -> lod $lodLevel" }
        updateInterpolation()
    }

    private fun updateInterpolation() {
        val lowerIndex = lodLevel.toInt().coerceIn(0, lodLevels.size - 1)
        val upperIndex = (lowerIndex + 1).coerceIn(0, lodLevels.size - 1)

        lowerLevel = lodLevels[lowerIndex]
        upperLevel = lodLevels[upperIndex]
        interpolationFactor = lodLevel - lowerIndex
    }

    override fun isVisible(state: State): Boolean {
        return state in lowerLevel.steinerTreeNodes || state in upperLevel.steinerTreeNodes
    }

    override fun getOpacity(state: State): Float {
        val inLower = state in lowerLevel.steinerTreeNodes
        val inUpper = state in upperLevel.steinerTreeNodes

        return when {
            inLower && inUpper -> 1f
            inLower && !inUpper -> 1f - smoothStep(interpolationFactor)
            !inLower && inUpper -> smoothStep(interpolationFactor)
            else -> 0f
        }
    }

    /** ease-in-out */
    private fun smoothStep(t: Float): Float {
        return t * t * (3f - 2f * t)
    }
}

