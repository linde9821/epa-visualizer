package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata

interface EpaFilter<T : Comparable<T>> {
    fun apply(epa: ExtendedPrefixAutomata<T>): ExtendedPrefixAutomata<T>
}
