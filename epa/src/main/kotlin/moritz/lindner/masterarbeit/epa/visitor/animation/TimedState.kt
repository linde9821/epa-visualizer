package moritz.lindner.masterarbeit.epa.visitor.animation

import moritz.lindner.masterarbeit.epa.domain.State

data class TimedState<T : Comparable<T>>(
    val state: State,
    val from: T,
    var to: T? = null, // null means still active
)
