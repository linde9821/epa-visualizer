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
    private val prettyPrint: Boolean = false
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
     * Returns the JSON representation of the automaton.
     * Must be called after the visitor has completed traversal.
     */
    fun toJson(): String {
        // Pre-compute all state keys once
        val stateKeyCache = mutableMapOf<State, String>()

        fun cachedStateToKey(state: State): String {
            return stateKeyCache.getOrPut(state) {
                when (state) {
                    is State.Root -> "root"
                    is State.PrefixState -> {
                        // Build path iteratively, not recursively
                        val path = mutableListOf<String>()
                        var current: State = state
                        while (current is State.PrefixState) {
                            path.add(current.via.name)
                            current = current.from
                        }
                        path.reverse()
                        path.joinToString("->", prefix = "root->")
                    }
                }
            }
        }

        // Single iteration to build all collections
        val jsonStates = mutableSetOf<JsonState>()
        val partitionByState = mutableMapOf<String, Int>()
        val sequenceByState = mutableMapOf<String, Set<JsonEvent>>()

        epa.states.forEach { state ->
            val key = cachedStateToKey(state)

            jsonStates.add(stateToJson(state, key)) // Pass pre-computed key
            partitionByState[key] = epa.partition(state)
            sequenceByState[key] = epa.sequence(state).map { event ->
                JsonEvent(
                    timestamp = event.timestamp.toString(),
                    activity = event.activity.toString(),
                    caseIdentifier = event.caseIdentifier
                )
            }.toSet()
        }

        val jsonAutomaton = JsonAutomaton(
            eventLogName = epa.eventLogName,
            states = jsonStates,
            activities = epa.activities.map { it.name }.toSet(),
            transitions = epa.transitions.map { transition ->
                JsonTransition(
                    start = cachedStateToKey(transition.start),
                    end = cachedStateToKey(transition.end),
                    activity = transition.activity.name
                )
            }.toSet(),
            partitionByState = partitionByState,
            sequenceByState = sequenceByState
        )

        return json.encodeToString(jsonAutomaton)
    }

    private fun stateToJson(state: State, precomputedKey: String): JsonState = when (state) {
        is State.Root -> JsonState(type = "root", name = state.name)
        is State.PrefixState -> JsonState(
            type = "prefix",
            name = state.name,
            from = precomputedKey.substringBeforeLast("->"),
            via = state.via.name
        )
    }
}

