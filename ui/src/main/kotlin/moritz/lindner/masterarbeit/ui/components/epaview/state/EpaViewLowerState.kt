package moritz.lindner.masterarbeit.ui.components.epaview.state

sealed class EpaViewLowerState {
    data object Animation : EpaViewLowerState()

    data object Statistics : EpaViewLowerState()

    data object None : EpaViewLowerState()
}
