package moritz.lindner.masterarbeit.epa.construction.builder

fun interface EpaProgressCallback {
    fun onProgress(current: Long, total: Long, task: String)
}