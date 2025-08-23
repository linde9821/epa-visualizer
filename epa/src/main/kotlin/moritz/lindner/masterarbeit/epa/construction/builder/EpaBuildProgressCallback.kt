package moritz.lindner.masterarbeit.epa.construction.builder

fun interface EpaBuildProgressCallback {
    fun onProgress(current: Long, total: Long, task: String)
}