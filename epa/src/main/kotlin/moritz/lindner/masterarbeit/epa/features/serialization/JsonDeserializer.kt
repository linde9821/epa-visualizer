package moritz.lindner.masterarbeit.epa.features.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition

/**
 * Deserializes an ExtendedPrefixAutomaton from JSON format.
 *
 * @param T The timestamp type used in the automaton's events.
 * @param timestampParser Function to parse timestamp strings back to type T
 */
class JsonDeserializer<T : Comparable<T>>(
    private val timestampParser: (String) -> T
) {

    private val json = Json {
        ignoreUnknownKeys = true
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
        val type: String,
        val name: String,
        val from: String? = null,
        val via: String? = null
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
     * Deserializes JSON string to ExtendedPrefixAutomaton
     */
    fun fromJson(jsonString: String): ExtendedPrefixAutomaton<T> {
        val jsonAutomaton = json.decodeFromString<JsonAutomaton>(jsonString)

        // Build state lookup for reconstruction
        val statesByKey = mutableMapOf<String, State>()

        // First pass: Create all states (start with root, then build dependencies)
        val jsonStatesByKey = jsonAutomaton.states.associateBy { stateKey(it) }

        fun buildState(key: String): State {
            if (statesByKey.containsKey(key)) {
                return statesByKey[key]!!
            }

            val jsonState = jsonStatesByKey[key]
                ?: throw IllegalArgumentException("State with key '$key' not found")

            val state = when (jsonState.type) {
                "root" -> State.Root
                "prefix" -> {
                    val fromKey = jsonState.from
                        ?: throw IllegalArgumentException("PrefixState must have 'from' field")
                    val viaName = jsonState.via
                        ?: throw IllegalArgumentException("PrefixState must have 'via' field")

                    val fromState = buildState(fromKey)
                    val viaActivity = Activity(viaName)

                    State.PrefixState(fromState, viaActivity)
                }
                else -> throw IllegalArgumentException("Unknown state type: ${jsonState.type}")
            }

            statesByKey[key] = state
            return state
        }

        // Build all states
        jsonAutomaton.states.forEach { jsonState ->
            buildState(stateKey(jsonState))
        }

        // Create activities
        val activities = jsonAutomaton.activities.map { Activity(it) }.toSet()

        // Create transitions
        val transitions = jsonAutomaton.transitions.map { jsonTransition ->
            val startState = statesByKey[jsonTransition.start]
                ?: throw IllegalArgumentException("Start state '${jsonTransition.start}' not found")
            val endState = statesByKey[jsonTransition.end]
                ?: throw IllegalArgumentException("End state '${jsonTransition.end}' not found")
            val activity = Activity(jsonTransition.activity)

            Transition(startState, activity, endState)
        }.toSet()

        // Create partition mapping
        val partitionByState = jsonAutomaton.partitionByState.mapKeys { (key, _) ->
            statesByKey[key] ?: throw IllegalArgumentException("State '$key' not found for partition mapping")
        }

        // Create sequence mapping
        val sequenceByState = jsonAutomaton.sequenceByState.mapKeys { (key, _) ->
            statesByKey[key] ?: throw IllegalArgumentException("State '$key' not found for sequence mapping")
        }.mapValues { (_, jsonEvents) ->
            jsonEvents.map { jsonEvent ->
                Event(
                    timestamp = timestampParser(jsonEvent.timestamp),
                    activity = Activity(jsonEvent.activity),
                    caseIdentifier = jsonEvent.caseIdentifier
                )
            }.toSet()
        }

        return ExtendedPrefixAutomaton(
            eventLogName = jsonAutomaton.eventLogName,
            states = statesByKey.values.toSet(),
            activities = activities,
            transitions = transitions,
            partitionByState = partitionByState,
            sequenceByState = sequenceByState
        )
    }

    /**
     * Generate the same key format used in serialization
     */
    private fun stateKey(jsonState: JsonState): String = when (jsonState.type) {
        "root" -> "root"
        "prefix" -> "${jsonState.from}->${jsonState.via}"
        else -> throw IllegalArgumentException("Unknown state type: ${jsonState.type}")
    }
}