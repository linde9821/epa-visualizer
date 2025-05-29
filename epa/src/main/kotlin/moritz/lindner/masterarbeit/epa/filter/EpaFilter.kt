package moritz.lindner.masterarbeit.epa.filter

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata

fun interface EpaFilter<T : Comparable<T>> {
    fun apply(epa: ExtendedPrefixAutomata<T>): ExtendedPrefixAutomata<T>

    fun then(next: EpaFilter<T>): EpaFilter<T> =
        EpaFilter { epa ->
            next.apply(this@EpaFilter.apply(epa))
        }

    companion object {
        fun <T : Comparable<T>> combine(filters: List<EpaFilter<T>>): EpaFilter<T> =
            filters.reduce { a, b ->
                a.then(b)
            }
    }
}

class DoNothingFilter<T : Comparable<T>> : EpaFilter<T> {
    override fun apply(epa: ExtendedPrefixAutomata<T>): ExtendedPrefixAutomata<T> = epa
}
