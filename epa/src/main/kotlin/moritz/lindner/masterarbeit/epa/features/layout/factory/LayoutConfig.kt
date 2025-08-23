package moritz.lindner.masterarbeit.epa.features.layout.factory

sealed class LayoutConfig(val name: String) {
    abstract fun getParameters(): Map<String, ParameterInfo>
    abstract fun updateParameter(name: String, value: Float): LayoutConfig

    data class Walker(
        val distance: Float = 20.0f,
        val yDistance: Float = 50.0f
    ) : LayoutConfig("Walker") {
        override fun getParameters() = mapOf(
            "distance" to ParameterInfo("Distance", 0.1f, 100.0f),
            "yDistance" to ParameterInfo("Y Distance", 10.0f, 200.0f, 5.0f)
        )

        override fun updateParameter(name: String, value: Float) = when (name) {
            "distance" -> copy(distance = value)
            "yDistance" -> copy(yDistance = value)
            else -> this
        }
    }

    data class RadialWalker(
        val layerSpace: Float = 50.0f,
        val margin: Float = 5.0f,
        val rotation: Float = 90.0f
    ) : LayoutConfig("Radial Walker") {
        override fun getParameters() = mapOf(
            "layerSpace" to ParameterInfo("Layer Space", 10.0f, 200.0f, 5.0f),
            "margin" to ParameterInfo("Margin (in Degrees)", 0.0f, 360.0f, 0.1f),
            "rotation" to ParameterInfo("Rotation", 0.0f, 360.0f, 1.0f)
        )

        override fun updateParameter(name: String, value: Float) = when (name) {
            "layerSpace" -> copy(layerSpace = value)
            "margin" -> copy(margin = value)
            "rotation" -> copy(rotation = value)
            else -> this
        }
    }

    data class DirectAngular(
        val layerSpace: Float = 50.0f,
        val rotation: Float = 0.0f
    ) : LayoutConfig("Direct Angular") {
        override fun getParameters() = mapOf(
            "layerSpace" to ParameterInfo("Layer Space", 10.0f, 200.0f, 5.0f),
            "rotation" to ParameterInfo("Rotation", 0.0f, 360.0f, 1.0f)
        )

        override fun updateParameter(name: String, value: Float) = when (name) {
            "layerSpace" -> copy(layerSpace = value)
            "rotation" -> copy(rotation = value)
            else -> this
        }
    }
}