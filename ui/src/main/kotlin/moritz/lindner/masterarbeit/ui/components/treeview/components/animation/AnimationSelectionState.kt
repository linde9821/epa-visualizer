package moritz.lindner.masterarbeit.ui.components.treeview.components.animation

sealed class AnimationSelectionState {
    data object NothingSelected : AnimationSelectionState()

    data object WholeLog : AnimationSelectionState()

    data object SingleCase : AnimationSelectionState()
}
