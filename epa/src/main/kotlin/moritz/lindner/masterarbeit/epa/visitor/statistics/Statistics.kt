package moritz.lindner.masterarbeit.epa.visitor.statistics

import moritz.lindner.masterarbeit.epa.domain.Activity

data class Statistics(
    val eventCount: Int,
    val caseCount: Int,
    val activityCount: Int,
    val stateCount: Int,
    val partitionsCount: Int,
    val activityFrequency: Map<Activity, Int>,
)
