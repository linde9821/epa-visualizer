package moritz.lindner.masterarbeit.epa.features.layout.factory

sealed class ParameterInfo {

    data class ColorPaletteListParameterInfo(
        val name: String,
        val selectionOptions: List<String>
    ) : ParameterInfo()

    data class NumberParameterInfo<T : Number>(
        val name: String,
        val min: T,
        val max: T,
        val steps: T,
    ) : ParameterInfo()

    data class BooleanParameterInfo(
        val name: String,
    ) : ParameterInfo()

    data class EnumParameterInfo<E : Enum<E>>(
        val name: String,
        val selectionOptions: List<E>
    ) : ParameterInfo()
}
