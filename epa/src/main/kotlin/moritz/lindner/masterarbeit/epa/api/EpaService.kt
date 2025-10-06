package moritz.lindner.masterarbeit.epa.api

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import moritz.lindner.masterarbeit.epa.features.animation.EventsByCasesCollector
import moritz.lindner.masterarbeit.epa.features.cycletime.CycleTime
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequency
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequencyVisitor
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequency
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequencyVisitor
import moritz.lindner.masterarbeit.epa.features.statistics.Statistics
import moritz.lindner.masterarbeit.epa.features.statistics.StatisticsVisitor
import moritz.lindner.masterarbeit.epa.features.traces.TraceAccessIndex

/**
 * Service for analyzing and manipulating Extended Prefix Automatons.
 *
 * @param T The type of timestamps used in the event log.
 */
class EpaService<T : Comparable<T>> {

    private val logger = KotlinLogging.logger { }

    /**
     * Computes general statistics for the EPA.
     *
     * @param epa The Extended Prefix Automaton to analyze.
     * @return Statistics containing event counts, case counts, and activity
     *    frequencies.
     */
    fun getStatistics(epa: ExtendedPrefixAutomaton<T>): Statistics<T> {
        val visitor = StatisticsVisitor<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    /**
     * Applies a list of filters to the EPA sequentially.
     *
     * @param epa The Extended Prefix Automaton to filter.
     * @param filters The list of filters to apply in order.
     * @return A new filtered Extended Prefix Automaton.
     */
    fun applyFilters(
        epa: ExtendedPrefixAutomaton<T>,
        filters: List<EpaFilter<T>>,
        progressCallback: EpaProgressCallback? = null
    ): ExtendedPrefixAutomaton<T> {
        return filters.fold(epa) { acc, filter ->
            logger.info { "Applying filter ${filter.name}" }
            filter.apply(acc.copy(), progressCallback)
        }
    }

    /**
     * Groups all events by their case identifiers.
     *
     * @param epa The Extended Prefix Automaton to analyze.
     * @return A map from case ID to list of events for that case.
     */
    fun getEventsByCase(epa: ExtendedPrefixAutomaton<T>): Map<String, List<Event<T>>> {
        val visitor = EventsByCasesCollector<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    /**
     * Computes normalized frequency statistics for each state.
     *
     * @param epa The Extended Prefix Automaton to analyze.
     * @return Normalized state frequency data.
     */
    fun getNormalizedStateFrequency(epa: ExtendedPrefixAutomaton<T>): NormalizedStateFrequency {
        val visitor = NormalizedStateFrequencyVisitor<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    /**
     * Computes normalized frequency statistics for each partition.
     *
     * @param epa The Extended Prefix Automaton to analyze.
     * @return Normalized partition frequency data.
     */
    fun getNormalizedPartitionFrequency(epa: ExtendedPrefixAutomaton<T>): NormalizedPartitionFrequency {
        val visitor = NormalizedPartitionFrequencyVisitor<T>()
        epa.acceptDepthFirst(visitor)
        return visitor.build()
    }

    fun filterNames(filters: List<EpaFilter<T>>): String {
        return filters.joinToString { it.name }
    }

    fun outgoingTransitions(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        selectedState: State
    ): Set<Transition> {
        return extendedPrefixAutomaton.transitions.filter { it.start == selectedState }.toSet()
    }

    fun incomingTransitions(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        selectedState: State
    ): Set<Transition> {
        return extendedPrefixAutomaton.transitions.filter { it.end == selectedState }.toSet()
    }

    fun getPathFromRoot(
        state: State,
    ): List<State> {
        return traverseToRoot(emptyList(), state).reversed()
    }

    fun getDepth(
        state: State,
    ): Int {
        return getPathFromRoot(state).size - 1
    }

    private tailrec fun traverseToRoot(
        acc: List<State>,
        current: State
    ): List<State> {
        return when (current) {
            is State.PrefixState -> traverseToRoot(acc + current, current.from)
            State.Root -> acc + listOf(current)
        }
    }

    fun getTracesByState(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State
    ): List<List<Event<T>>> {
        val seq = extendedPrefixAutomaton.sequence(state)

        val traces = TraceAccessIndex<T>()
        extendedPrefixAutomaton.acceptDepthFirst(traces)

        return seq.map { event ->
            traces.getTraceByEvent(event)
        }
    }

    fun computeCycleTimes(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        state: State,
        minus: (T, T) -> T
    ): List<T> {
        val cycleTime = CycleTime<T>()
        extendedPrefixAutomaton.acceptDepthFirst(cycleTime)

        return cycleTime.cycleTimesOfState(state, minus)
    }

    fun getStateByEvent(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>): Map<Event<T>, State> {
        val seqByState = extendedPrefixAutomaton.states
            .map { state -> state to extendedPrefixAutomaton.sequence(state) }
        return seqByState
            .flatMap { (key, values) ->
                values.map { value -> value to key }
            }.toMap()
    }
}
