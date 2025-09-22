package moritz.lindner.masterarbeit.epa.api

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.animation.EventLogAnimation
import moritz.lindner.masterarbeit.epa.features.animation.SingleCaseAnimationBuilder
import moritz.lindner.masterarbeit.epa.features.animation.WholeEventLogAnimationBuilder

class AnimationService<T : Comparable<T>> {

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

    fun createCaseAnimation(epa: ExtendedPrefixAutomaton<T>, caseId: String): EventLogAnimation<T> {
        val builder = SingleCaseAnimationBuilder<T>(caseId)
        epa.acceptDepthFirst(builder)
        return builder.build()
    }
}