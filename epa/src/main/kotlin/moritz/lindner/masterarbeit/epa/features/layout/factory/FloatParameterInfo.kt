package moritz.lindner.masterarbeit.epa.features.layout.factory

data class ParameterInfo(
    val name: String,
    val min: Float,
    val max: Float,
    val step: Float = 0.1f
)