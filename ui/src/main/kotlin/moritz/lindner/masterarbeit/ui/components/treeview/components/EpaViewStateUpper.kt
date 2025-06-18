package moritz.lindner.masterarbeit.ui.components.treeview.components

sealed class EpaViewStateUpper {
    data object Filter : EpaViewStateUpper()

    data object Layout : EpaViewStateUpper()

    data object None : EpaViewStateUpper()
}
