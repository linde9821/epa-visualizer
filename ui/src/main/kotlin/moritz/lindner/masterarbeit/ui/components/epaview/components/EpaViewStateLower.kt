package moritz.lindner.masterarbeit.ui.components.epaview.components

sealed class EpaViewStateLower {
    data object Animation : EpaViewStateLower()

    data object Statistics : EpaViewStateLower()

    data object None : EpaViewStateLower()
}
