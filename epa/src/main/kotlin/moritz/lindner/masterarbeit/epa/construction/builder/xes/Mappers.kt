package moritz.lindner.masterarbeit.epa.construction.builder.xes

import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XTrace
import org.deckfour.xes.model.impl.XAttributeLiteralImpl
import org.deckfour.xes.model.impl.XAttributeTimestampImpl

class BPI2017ChallengeEventMapper : XESEventLogMapper<Long>("Challenge 2017") {
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

class BPI2017OfferChallengeEventMapper : XESEventLogMapper<Long>("Challenge Offer 2017") {
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

class BPI2018ChallengeMapper : XESEventLogMapper<Long>("Challenge 2018") {
    override fun map(
        xEvent: XEvent,
        xTrace: XTrace,
    ): Event<Long> =
        Event(
            activity = Activity(
                ((xEvent.attributes["doctype"] as XAttributeLiteralImpl).value.toString()) + " - " +
                        (xEvent.attributes["subprocess"] as XAttributeLiteralImpl).value.toString() + " - " +
                        (xEvent.attributes["concept:name"] as XAttributeLiteralImpl).value,
            ),
            timestamp = (xEvent.attributes["time:timestamp"] as XAttributeTimestampImpl).value.time,
            caseIdentifier = ((xTrace.attributes["concept:name"] as XAttributeLiteralImpl).value.toString()),
        )
}

class SampleEventMapper : XESEventLogMapper<Long>("Sample") {
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

class BPI2020 : XESEventLogMapper<Long>("BPI 2020") {
    override fun map(
        xEvent: XEvent,
        xTrace: XTrace
    ): Event<Long> {
        return Event(
            activity = Activity((xEvent.attributes["concept:name"] as XAttributeLiteralImpl).value),
            timestamp = (xEvent.attributes["time:timestamp"] as XAttributeTimestampImpl).value.time,
            caseIdentifier = (xTrace.attributes["id"] as XAttributeLiteralImpl).value,
        )
    }
}

object Mappers {
    private val mappers = listOf(
        SampleEventMapper(),
        BPI2017OfferChallengeEventMapper(),
        BPI2017ChallengeEventMapper(),
        BPI2018ChallengeMapper(),
        BPI2020()
    )

    private val mappersByName = Mappers.getMappers().associateBy { it.name }

    fun getMappers(): List<XESEventLogMapper<Long>> {
        return mappers
    }

    fun getMappersByName() = mappersByName
}