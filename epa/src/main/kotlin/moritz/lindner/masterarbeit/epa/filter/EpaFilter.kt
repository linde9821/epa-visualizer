package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata

fun interface EpaFilter<T : Comparable<T>> {
    fun apply(epa: ExtendedPrefixAutomata<T>): ExtendedPrefixAutomata<T>

    fun then(next: EpaFilter<T>): EpaFilter<T> = EpaFilter { next.apply(this.apply(it)) }
}
