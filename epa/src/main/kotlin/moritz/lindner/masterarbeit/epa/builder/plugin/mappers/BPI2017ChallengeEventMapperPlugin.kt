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
 * Event mapper plugin for the BPI 2017 Challenge log format.
 *
 * This mapper handles logs from the BPI 2017 Challenge, which have a specific structure
 * with "concept:name" attributes for activity names and case identifiers.
 */
class BPI2017ChallengeEventMapperPlugin : EventMapperPlugin<Long>() {
    override val id: String = "bpi2017-challenge"
    override val name: String = "BPI 2017 Challenge Event Mapper"
    override val description: String = "Maps events from the BPI 2017 Challenge log format, which uses 'concept:name' attributes for activity names and case identifiers."

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