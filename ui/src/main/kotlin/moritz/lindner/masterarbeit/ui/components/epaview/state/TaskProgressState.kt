package moritz.lindner.masterarbeit.ui.components.epaview.state

/** Represents the progress of a task within a tab */
data class TaskProgressState(
    val current: Long = 0,
    val total: Long = 0,
    val taskName: String = ""
) {
    val percentage: Float
        get() = if (total > 0) (current.toFloat() / total.toFloat()) else 0f

    val isComplete: Boolean
        get() = total in 1..current
}