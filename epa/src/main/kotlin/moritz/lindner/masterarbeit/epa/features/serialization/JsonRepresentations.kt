package moritz.lindner.masterarbeit.epa.features.serialization

import kotlinx.serialization.Serializable

@Serializable
data class JsonAutomaton(
    val eventLogName: String,
    val states: Set<JsonState>,
    val activities: Set<String>,
    val transitions: Set<JsonTransition>,
    val partitionByState: Map<String, Int>,
    val sequenceByState: Map<String, Set<JsonEvent>>
)

@Serializable
data class JsonState(
    val type: String,
    val name: String,
    val from: String? = null,
    val via: String? = null
)

@Serializable
data class JsonTransition(
    val start: String,
    val end: String,
    val activity: String
)

@Serializable
data class JsonEvent(
    val timestamp: String,
    val activity: String,
    val caseIdentifier: String
)