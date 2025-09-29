package moritz.lindner.masterarbeit.ui.components.epaview.state

sealed class EpaViewStateUpper {
    data object Filter : EpaViewStateUpper()

    data object Layout : EpaViewStateUpper()

    data object None : EpaViewStateUpper()

    data object Project : EpaViewStateUpper()

    data object Analysis : EpaViewStateUpper()

    data object NaturalLanguage : EpaViewStateUpper()
}
