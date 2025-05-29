package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata

interface EpaFilter<T : Comparable<T>> {
    fun apply(epa: ExtendedPrefixAutomata<T>): ExtendedPrefixAutomata<T>

    fun then(next: EpaFilter<T>): EpaFilter<T> =
        object : EpaFilter<T> {
            override fun apply(epa: ExtendedPrefixAutomata<T>): ExtendedPrefixAutomata<T> = next.apply(this.apply(epa))

            override fun name(): String = "Then"
        }

    fun name(): String
}

class DoNothingFilter<T : Comparable<T>> : EpaFilter<T> {
    override fun apply(epa: ExtendedPrefixAutomata<T>): ExtendedPrefixAutomata<T> = epa

    override fun name(): String = "DoNothing"
}
