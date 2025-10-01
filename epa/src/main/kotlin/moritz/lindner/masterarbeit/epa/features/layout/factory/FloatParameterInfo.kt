package moritz.lindner.masterarbeit.epa.features.layout.factory

sealed class ParameterInfo {

    data class FloatParameterInfo(
        val name: String,
        val min: Float,
        val max: Float,
        val step: Float = 0.1f,
    ): ParameterInfo()

    data class BooleanParameterInfo(
        val name: String,
    ): ParameterInfo()
}

