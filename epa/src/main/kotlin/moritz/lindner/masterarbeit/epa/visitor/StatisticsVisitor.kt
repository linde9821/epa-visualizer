package moritz.lindner.masterarbeit.epa.visitor

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import kotlin.math.max

class StatisticsVisitor<T : Comparable<T>>(
    private val extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
) : AutomataVisitor<T> {
    private val visitedStates = mutableSetOf<State>()
    private val visitedTransitions = mutableSetOf<Transition>()
    private var eventCount = 0
    private var partitions = 0
    private val cases = mutableSetOf<String>()
    private val activityFrequency = mutableMapOf<Activity, Int>()
    private val prefixLengths = mutableListOf<Int>()

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        state: State,
        depth: Int,
    ) {
        if (!visitedStates.add(state)) return

        partitions = max(partitions, extendedPrefixAutomata.partition(state))

        val sequence = extendedPrefixAutomata.sequence(state)
        prefixLengths += sequence.size
        for (event in sequence) {
            activityFrequency[event.activity] = activityFrequency.getOrDefault(event.activity, 0) + 1
        }
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        transition: Transition,
        depth: Int,
    ) {
        visitedTransitions.add(transition)
    }

    override fun visit(
        extendedPrefixAutomata: ExtendedPrefixAutomata<T>,
        event: Event<T>,
        depth: Int,
    ) {
        cases.add(event.caseIdentifier)
        eventCount++
    }

    fun report(): String {
        val totalStates = visitedStates.size
        val totalTransitions = visitedTransitions.size
        val totalEvents = eventCount

        val minPrefix = prefixLengths.minOrNull() ?: 0
        val maxPrefix = prefixLengths.maxOrNull() ?: 0
        val avgPrefix = prefixLengths.average().takeIf { prefixLengths.isNotEmpty() } ?: 0.0

        return buildString {
            appendLine("\nAutomata Statistics:")
            appendLine("  Events:       $totalEvents")
            appendLine("  Cases:        ${cases.size}")
            appendLine("  Activities:   ${activityFrequency.values.size}")
            appendLine("  Partitions:   $partitions")
            appendLine("  States:       $totalStates")
            appendLine("  Transitions:  $totalTransitions")
            appendLine("  Prefix length: min=$minPrefix, max=$maxPrefix, avg=%.2f".format(avgPrefix))
            appendLine("  Activity frequency:")
            for ((activity, count) in activityFrequency.entries.sortedByDescending { it.value }) {
                appendLine("    ${activity.name}: ${count / totalEvents.toFloat() * 100.0}% ($count)")
            }
            appendLine("  minimal frequency: ${activityFrequency.minBy { it.value }.value}")
            val median =
                if (activityFrequency.size % 2 ==
                    0
                ) {
                    activityFrequency.values.sorted()[(activityFrequency.values.size + 1) / 2]
                } else {
                    activityFrequency.values.sorted()[
                        (
                            activityFrequency.size / 2
                        ),
                    ]
                }
            appendLine("  median frequency: $median")
            val mean = activityFrequency.values.sum() / activityFrequency.values.size.toFloat()
            appendLine("  mean frequency: $mean")
            appendLine("  max frequency: ${activityFrequency.maxBy { it.value }.value}")
        }
    }
}
