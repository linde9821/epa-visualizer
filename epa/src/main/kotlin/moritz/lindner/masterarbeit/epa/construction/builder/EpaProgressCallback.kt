package moritz.lindner.masterarbeit.epa.construction.builder

fun interface EpaProgressCallback {
    fun onProgress(current: Long, total: Long, task: String)

    fun onProgress(current: Int, total: Int, task: String) {
        onProgress(current.toLong(), total.toLong(), task)
    }
}