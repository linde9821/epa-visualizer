package moritz.lindner.masterarbeit.epa.features.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.State

/**
 * A simple visitor that serializes an ExtendedPrefixAutomaton to JSON format,
 * preserving only the core data structure.
 */
class JsonSerialization<T : Comparable<T>>(
    private val epa: ExtendedPrefixAutomaton<T>,
    private val prettyPrint: Boolean = true
) {

    private val json = Json {
        prettyPrint = this@JsonSerialization.prettyPrint
        encodeDefaults = true
    }

    @Serializable
    private data class JsonAutomaton(
        val eventLogName: String,
        val states: Set<JsonState>,
        val activities: Set<String>,
        val transitions: Set<JsonTransition>,
        val partitionByState: Map<String, Int>,
        val sequenceByState: Map<String, Set<JsonEvent>>
    )

    @Serializable
    private data class JsonState(
        val type: String, // "root" or "prefix"
        val name: String,
        val from: String? = null, // Only for PrefixState
        val via: String? = null   // Only for PrefixState
    )

    @Serializable
    private data class JsonTransition(
        val start: String,
        val end: String,
        val activity: String
    )

    @Serializable
    private data class JsonEvent(
        val timestamp: String,
        val activity: String,
        val caseIdentifier: String
    )

    /**
     * Converts a State to its string representation for use as keys
     */
    private fun stateToKey(state: State): String = when (state) {
        is State.PrefixState -> "${stateToKey(state.from)}->${state.via.name}"
        State.Root -> "root"
    }

    /**
     * Converts a State to JsonState
     */
    private fun stateToJson(state: State): JsonState = when (state) {
        is State.Root -> JsonState(
            type = "root",
            name = state.name
        )
        is State.PrefixState -> JsonState(
            type = "prefix",
            name = state.name,
            from = stateToKey(state.from),
            via = state.via.name
        )
    }

    /**
     * Returns the JSON representation of the automaton.
     * Must be called after the visitor has completed traversal.
     */
    fun toJson(): String {
        val jsonAutomaton = JsonAutomaton(
            eventLogName = epa.eventLogName,
            states = epa.states.map { stateToJson(it) }.toSet(),
            activities = epa.activities.map { it.name }.toSet(),
            transitions = epa.transitions.map { transition ->
                JsonTransition(
                    start = stateToKey(transition.start),
                    end = stateToKey(transition.end),
                    activity = transition.activity.name
                )
            }.toSet(),
            partitionByState = epa.states.associate { state ->
                stateToKey(state) to epa.partition(state)
            },
            sequenceByState = epa.states.associate { state ->
                stateToKey(state) to epa.sequence(state).map { event ->
                    JsonEvent(
                        timestamp = event.timestamp.toString(),
                        activity = event.activity.toString(),
                        caseIdentifier = event.caseIdentifier
                    )
                }.toSet()
            }
        )
        return json.encodeToString(jsonAutomaton)
    }
}

