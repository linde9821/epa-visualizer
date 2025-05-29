package moritz.lindner.masterarbeit.epa.visitor.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitor

/**
frequency of the amount of events the state has seen of a type by the total amount of the event type
 */
class NormalizedPartitionFrequencyVisitor<T : Comparable<T>> : AutomataVisitor<T> {
    private val relativeFrequencyByPartition = HashMap<Int, Float>()
    private var allEvents = 0

    fun frequencyByPartition(c: Int): Float = relativeFrequencyByPartition[c]!!

    override fun onEnd(extendedPrefixAutomata: ExtendedPrefixAutomata<T>) {
        val statesByPartition = extendedPrefixAutomata.states.groupBy { extendedPrefixAutomata.partition(it) }

        val frequencyByPartition =
            statesByPartition.mapValues { (partition, states) ->
                states.sumOf { extendedPrefixAutomata.sequence(it).size }
            }

        relativeFrequencyByPartition.putAll(
            frequencyByPartition.mapValues { (partition, frequency) ->
                frequency.toFloat() / allEvents
            },
        )
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        event: Event<T>,
        depth: Int,
    ) {
        allEvents++
    }

    fun min(): Float = relativeFrequencyByPartition.values.min()

    fun max(): Float = relativeFrequencyByPartition.values.max()
}
