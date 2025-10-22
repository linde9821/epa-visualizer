package moritz.lindner.masterarbeit.epa.features.layout.factory

sealed class ParameterInfo {

    data class NumberParameterInfo<T : Number>(
        val name: String,
        val min: T,
        val max: T,
        val step: T,
    ) : ParameterInfo()

    data class BooleanParameterInfo(
        val name: String,
    ) : ParameterInfo()
}

