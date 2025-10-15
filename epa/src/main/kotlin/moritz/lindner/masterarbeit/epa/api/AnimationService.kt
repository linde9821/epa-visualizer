package moritz.lindner.masterarbeit.epa.api

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.animation.EventLogAnimation
import moritz.lindner.masterarbeit.epa.features.animation.SingleCaseAnimationBuilder
import moritz.lindner.masterarbeit.epa.features.animation.WholeEventLogAnimationBuilder

/**
 * Service for creating animations from Extended Prefix Automatons.
 *
 * @param T The type of timestamps used in the event log.
 */
class AnimationService<T : Comparable<T>> {

    /**
     * Creates an animation for the entire event log.
     *
     * @param epa The Extended Prefix Automaton to animate.
     * @param epsilon The value added to the last timestamp to close open-ended
     *    intervals.
     * @param increment Function to increment timestamps.
     * @return An EventLogAnimation for the complete log.
     */
    fun createFullLogAnimation(
        epa: ExtendedPrefixAutomaton<T>,
        epsilon: T,
        increment: (T, T) -> T,
    ): EventLogAnimation<T> {
        val builder = WholeEventLogAnimationBuilder<T>(epa.eventLogName)
        epa.acceptDepthFirst(builder)
        return builder.build(
            epsilon = epsilon,
            increment = increment
        )
    }

    /**
     * Creates an animation for a single case.
     *
     * @param epa The Extended Prefix Automaton containing the case.
     * @param caseId The ID of the case to animate.
     * @return An EventLogAnimation for the specified case.
     */
    fun createCaseAnimation(epa: ExtendedPrefixAutomaton<T>, caseId: String): EventLogAnimation<T> {
        val builder = SingleCaseAnimationBuilder<T>(caseId)
        epa.acceptDepthFirst(builder)
        return builder.build()
    }
}