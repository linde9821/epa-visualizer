package moritz.lindner.masterarbeit.epa.features.layout.factory

sealed class LayoutConfig(val name: String) {

    abstract val render: Boolean

    abstract fun getParameters(): Map<String, ParameterInfo>
    abstract fun updateParameter(name: String, value: Any): LayoutConfig

    data class Walker(
        val distance: Float = 20.0f,
        val yDistance: Float = 50.0f,
        override val render: Boolean = true,
    ) : LayoutConfig("Walker") {
        override fun getParameters() = mapOf(
            "distance" to ParameterInfo.FloatParameterInfo("Distance", 0.1f, 100.0f),
            "yDistance" to ParameterInfo.FloatParameterInfo("Y Distance", 10.0f, 200.0f, 5.0f),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled")
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "distance" -> copy(distance = value as Float)
            "yDistance" -> copy(yDistance = value as Float)
            "enabled" -> copy(render = value as Boolean)
            else -> this
        }
    }

    data class RadialWalker(
        val layerSpace: Float = 120.0f,
        val margin: Float = 5.0f,
        val rotation: Float = 90.0f,
        override val render: Boolean = true,
    ) : LayoutConfig("Radial Walker") {
        override fun getParameters() = mapOf(
            "layerSpace" to ParameterInfo.FloatParameterInfo("Layer Space", 10.0f, 300.0f, 5.0f),
            "margin" to ParameterInfo.FloatParameterInfo("Margin (in Degrees)", 0.0f, 360.0f, 0.1f),
            "rotation" to ParameterInfo.FloatParameterInfo("Rotation", 0.0f, 360.0f, 1.0f),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled")
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "layerSpace" -> copy(layerSpace = value as Float)
            "margin" -> copy(margin = value as Float)
            "rotation" -> copy(rotation = value as Float)
            "enabled" -> copy(render = value as Boolean)
            else -> this
        }
    }

    data class DirectAngular(
        val layerSpace: Float = 50.0f,
        val rotation: Float = 0.0f,
        override val render: Boolean = true,
    ) : LayoutConfig("Direct Angular") {
        override fun getParameters() = mapOf(
            "layerSpace" to ParameterInfo.FloatParameterInfo("Layer Space", 10.0f, 200.0f, 5.0f),
            "rotation" to ParameterInfo.FloatParameterInfo("Rotation", 0.0f, 360.0f, 1.0f),
            "enabled" to ParameterInfo.BooleanParameterInfo("Enabled")
        )

        override fun updateParameter(name: String, value: Any) = when (name) {
            "layerSpace" -> copy(layerSpace = value as Float)
            "rotation" -> copy(rotation = value as Float)
            "enabled" -> copy(render = value as Boolean)
            else -> this
        }
    }
}