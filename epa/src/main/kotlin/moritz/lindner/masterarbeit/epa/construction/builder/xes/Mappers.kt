package moritz.lindner.masterarbeit.epa.construction.builder.xes

import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XTrace
import org.deckfour.xes.model.impl.XAttributeLiteralImpl
import org.deckfour.xes.model.impl.XAttributeTimestampImpl

class BPI2017ChallengeEventMapper : EventLogMapper<Long>("Challenge 2017") {
    override fun map(
        xEvent: XEvent,
        xTrace: XTrace,
    ): Event<Long> =
        Event(
            activity = Activity((xEvent.attributes["concept:name"] as XAttributeLiteralImpl).value),
            timestamp = (xEvent.attributes["time:timestamp"] as XAttributeTimestampImpl).value.time,
            caseIdentifier = (xTrace.attributes["concept:name"] as XAttributeLiteralImpl).value,
        )
}

class BPI2017OfferChallengeEventMapper : EventLogMapper<Long>("Challenge Offer 2017") {
    override fun map(
        xEvent: XEvent,
        xTrace: XTrace,
    ): Event<Long> =
        Event(
            activity = Activity((xEvent.attributes["concept:name"] as XAttributeLiteralImpl).value),
            timestamp = (xEvent.attributes["time:timestamp"] as XAttributeTimestampImpl).value.time,
            caseIdentifier = (xTrace.attributes["ApplicationID"] as XAttributeLiteralImpl).value,
        )
}

class BPI2018ChallengeMapper : EventLogMapper<Long>("Challenge 2018") {
    override fun map(
        xEvent: XEvent,
        xTrace: XTrace,
    ): Event<Long> =
        Event(
            activity =
                Activity(
                    ((xEvent.attributes["doctype"] as XAttributeLiteralImpl).value.toString()) + " - " +
                            (xEvent.attributes["subprocess"] as XAttributeLiteralImpl).value.toString() + " - " +
                            (xEvent.attributes["concept:name"] as XAttributeLiteralImpl).value,
                ),
            timestamp = (xEvent.attributes["time:timestamp"] as XAttributeTimestampImpl).value.time,
            caseIdentifier = ((xTrace.attributes["concept:name"] as XAttributeLiteralImpl).value.toString()),
        )
}

class SampleEventMapper : EventLogMapper<Long>("Sample") {
    override fun map(
        xEvent: XEvent,
        xTrace: XTrace,
    ): Event<Long> =
        Event(
            activity = Activity((xEvent.attributes["concept:name"] as XAttributeLiteralImpl).value),
            timestamp = (xEvent.attributes["time:timestamp"] as XAttributeTimestampImpl).value.time,
            caseIdentifier = (xTrace.attributes["concept:name"] as XAttributeLiteralImpl).value,
        )
}
