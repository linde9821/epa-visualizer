package moritz.lindner.masterarbeit.epa.builder.plugin.mappers

import moritz.lindner.masterarbeit.epa.builder.plugin.EventMapperPlugin
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace
import org.deckfour.xes.model.impl.XAttributeLiteralImpl
import org.deckfour.xes.model.impl.XAttributeTimestampImpl

/**
 * Event mapper plugin for sample event logs.
 *
 * This mapper handles simple event logs with a standard structure
 * using "concept:name" attributes for both activity names and case identifiers.
 * It's suitable for many standard XES logs that follow the IEEE XES standard.
 */
class SampleEventMapperPlugin : EventMapperPlugin<Long>() {
    override val id: String = "sample"
    override val name: String = "Sample Event Mapper"
    override val description: String = "Maps events from standard XES logs that use 'concept:name' attributes for both activity names and case identifiers."

    override fun map(
        xEvent: XEvent,
        xTrace: XTrace,
    ): Event<Long> =
        Event(
            activity = Activity((xEvent.attributes["concept:name"] as XAttributeLiteralImpl).value),
            timestamp = (xEvent.attributes["time:timestamp"] as XAttributeTimestampImpl).value.time,
            caseIdentifier = (xTrace.attributes["concept:name"] as XAttributeLiteralImpl).value,
        )

    override fun canHandle(log: XLog): Boolean {
        if (log.isEmpty()) return false
        
        // Check if the first trace has the expected attributes
        val firstTrace = log.first()
        if (firstTrace.isEmpty()) return false
        
        // Check if the trace has a "concept:name" attribute
        if (!firstTrace.attributes.containsKey("concept:name")) return false
        
        // Check if the first event has the expected attributes
        val firstEvent = firstTrace.first()
        return firstEvent.attributes.containsKey("concept:name") && 
               firstEvent.attributes.containsKey("time:timestamp")
    }
}