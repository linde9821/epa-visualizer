package moritz.lindner.masterarbeit.epa.domain

@JvmInline
value class Activity(
    val name: String,
) {
    override fun toString(): String = name
}
