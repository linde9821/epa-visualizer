package moritz.lindner.masterarbeit.epa.builder

import moritz.lindner.masterarbeit.epa.domain.Event
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XTrace

abstract class EventLogMapper<T : Comparable<T>> {
    fun build(log: Iterable<XTrace>): List<Event<T>> =
        log
            .flatMap { trace ->
                var previous: Event<T>? = null

                trace
                    .toList()
                    .map { event -> map(event, trace) }
                    .map { current ->
                        current.copy(predecessor = previous).also {
                            previous = current
                        }
                    }
            }.sortedBy { it.timestamp }

    abstract fun map(
        xEvent: XEvent,
        xTrace: XTrace,
    ): Event<T>
}
