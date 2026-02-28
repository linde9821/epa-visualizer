package moritz.lindner.masterarbeit.metrics.filter

data class FilterReport(
    val logName: String,
    val totalStates: Int,
    val totalEvents: Int,
    val filterName: String,
    val filterThreshold: Float,
    val eventsAfterFilter: Int,
    val statesAfterFilter: Int,
    val eventsPercentage: Float
) {
    fun toRow(): List<String> = listOf(
        logName,
        totalStates.toString(),
        totalEvents.toString(),
        filterName,
        filterThreshold.toString(),
        eventsAfterFilter.toString(),
        statesAfterFilter.toString(),
        "%.8f".format(eventsPercentage * 100.0)
    )
}