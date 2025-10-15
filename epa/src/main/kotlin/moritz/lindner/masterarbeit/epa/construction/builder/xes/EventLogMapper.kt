package moritz.lindner.masterarbeit.epa.construction.builder.xes

import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.Event
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XTrace

/**
 * Abstract class for mapping XES event logs into domain-specific [Event]
 * objects with comparable timestamps.
 *
 * Implementers must define how individual [XEvent]s are mapped into
 * [Event]s, including how to extract the timestamp and other metadata. The
 * mapper is responsible for enriching events with predecessor information
 * and sorting them chronologically.
 *
 * @param T The type used for timestamps (e.g., [Long],
 *    [java.time.LocalDateTime]), which must be [Comparable].
 */
abstract class EventLogMapper<T : Comparable<T>>(val name: String) {
    /**
     * Converts an iterable collection of [XTrace] objects into a
     * chronologically sorted list of [Event]s.
     *
     * Each event is mapped using [map], and index based predecessor, successor
     * relationships are established so that each event knows its immediate
     * predecessor, successor within its trace. The final output is a flat list
     * of all events from all traces, sorted by their timestamp.
     *
     * @param log The iterable collection of [XTrace]s representing the event
     *    log.
     * @return A list of [Event]s sorted by their [Event.timestamp], each with
     *    a reference to its predecessor.
     */
    fun build(log: Iterable<XTrace>, progressCallback: EpaProgressCallback? = null): List<Event<T>> {
        val logSize = log.toList().size.toLong()

        return log
            .toList()
            .flatMapIndexed { index, trace ->
                parseTrace(
                    trace = trace,
                    index = index,
                    logSize = logSize,
                    progressCallback = progressCallback
                )
            }.sortedBy { it.timestamp }
    }

    private fun parseTrace(
        trace: XTrace,
        index: Int,
        logSize: Long,
        progressCallback: EpaProgressCallback?,
    ): List<Event<T>> {
        return trace
            .map { event -> map(event, trace) }
            .sortedBy(Event<T>::timestamp)
            .also {
                progressCallback?.onProgress(
                    current = (index + 1).toLong(),
                    total = logSize,
                    task = "Convert traces to plain log (sorted list of events)"
                )
            }
    }

    /**
     * Maps a single [XEvent] and its containing [XTrace] into a
     * domain-specific [Event] object.
     *
     * Implementations should extract relevant information from the event
     * (such as activity name, timestamp, or case identifier) and return a new
     * [Event] instance.
     *
     * @param xEvent The XES event to be mapped.
     * @param xTrace The trace that contains the event.
     * @return A domain-specific [Event] representation of the given [XEvent].
     */
    abstract fun map(
        xEvent: XEvent,
        xTrace: XTrace,
    ): Event<T>
}
