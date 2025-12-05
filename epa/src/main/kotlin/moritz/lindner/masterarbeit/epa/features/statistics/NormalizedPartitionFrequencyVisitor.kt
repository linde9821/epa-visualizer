package moritz.lindner.masterarbeit.epa.features.statistics

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor

/**
 * Computes the normalized frequency of events per partition in an
 * [ExtendedPrefixAutomaton].
 *
 * The frequency is calculated as the number of events observed in each
 * partition divided by the total number of events across all partitions.
 * The result is a value in [0.0, 1.0].
 *
 * This visitor must be run using
 * [ExtendedPrefixAutomaton.acceptDepthFirst] or
 * [acceptBreadthFirst] before querying the frequencies.
 *
 * @param T The timestamp type used in the automaton's events.
 */
class NormalizedPartitionFrequencyVisitor<T : Comparable<T>>(
    private val progressCallback: EpaProgressCallback? = null
) : AutomatonVisitor<T> {
    private val relativeFrequencyByPartition = HashMap<Int, Float>()
    private var allEvents = 0

    private val epaService = EpaService<T>()

    override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>) {


        val terminatingStatesByPartition = extendedPrefixAutomaton
            .states
            .groupBy(extendedPrefixAutomaton::partition)
            .mapValues { (partition, states) ->

                // Edge Case: Undefined partition of root
                if (partition == 0) {
                    State.Root
                } else {
                    val terminatingStateOfPartitionOrRoot = states.filter { state ->
                        epaService.isTerminating(extendedPrefixAutomaton, state)                }

                    assert(terminatingStateOfPartitionOrRoot.size == 1) {
                        ""
                    }

                    terminatingStateOfPartitionOrRoot.first()
                }
            }

        val frequencyByPartition = terminatingStatesByPartition
            .mapValues { (_, state) ->
                epaService.getPathToRoot(state).sumOf { stateOnPath ->
                    extendedPrefixAutomaton.sequence(stateOnPath).size
                }
            }

        relativeFrequencyByPartition.putAll(
            frequencyByPartition.mapValues { (_, frequency) ->
                frequency.toFloat() / allEvents
            },
        )
    }

    override fun visit(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<T>,
        event: Event<T>,
        depth: Int,
    ) {
        allEvents++
    }

    override fun onProgress(current: Long, total: Long) {
        progressCallback?.onProgress(current, total, "Compute normalized frequency of events per partition")
    }

    fun build(): NormalizedPartitionFrequency {


        return NormalizedPartitionFrequency(relativeFrequencyByPartition)
    }
}
