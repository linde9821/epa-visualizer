package moritz.lindner.masterarbeit.epa.domain

data class Event<T : Comparable<T>>(
    val activity: Activity,
    val timestamp: T,
    val caseIdentifier: String,
    val predecessor: Event<T>? = null,
)
