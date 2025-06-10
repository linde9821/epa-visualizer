package moritz.lindner.masterarbeit.epa.builder.plugin

import moritz.lindner.masterarbeit.epa.builder.EventLogMapper
import moritz.lindner.masterarbeit.epa.domain.Event
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace

/**
 * Abstract base class for event mapper plugins that can be dynamically discovered and registered.
 *
 * This extends the base [EventLogMapper] functionality with metadata about the plugin
 * and methods to determine if it can handle a specific log format.
 *
 * @param T The type used for timestamps (e.g., [Long], [LocalDateTime]), which must be [Comparable].
 */
abstract class EventMapperPlugin<T : Comparable<T>> : EventLogMapper<T>() {
    /**
     * A unique identifier for this mapper plugin.
     */
    abstract val id: String

    /**
     * A human-readable name for this mapper plugin.
     */
    abstract val name: String

    /**
     * A description of what this mapper does and what log formats it supports.
     */
    abstract val description: String

    /**
     * Determines whether this mapper can handle the given XES log.
     *
     * Implementations should examine the log structure to determine if it matches
     * the expected format for this mapper.
     *
     * @param log The XES log to check.
     * @return true if this mapper can handle the log, false otherwise.
     */
    abstract fun canHandle(log: XLog): Boolean
}
