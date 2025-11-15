package moritz.lindner.masterarbeit.ui.components.epaview.state

sealed class EpaViewUpperState {
    data object Filter : EpaViewUpperState()

    data object Layout : EpaViewUpperState()

    data object None : EpaViewUpperState()

    data object Project : EpaViewUpperState()

    data object Details : EpaViewUpperState()
}
