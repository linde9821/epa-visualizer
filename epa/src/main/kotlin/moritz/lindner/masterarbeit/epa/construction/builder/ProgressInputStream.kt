package moritz.lindner.masterarbeit.epa.construction.builder

import java.io.InputStream

class ProgressInputStream(
    private val inputStream: InputStream,
    private val totalSize: Long,
    private val progressCallback: (bytesRead: Long, totalSize: Long, percentage: Float) -> Unit,
    private val updateIntervalMs: Long = 100
) : InputStream() {

    private var bytesRead: Long = 0
    private var lastUpdateTime: Long = 0

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val result = inputStream.read(buffer, offset, length)
        if (result > 0) {
            bytesRead += result

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= updateIntervalMs) {
                notifyProgress()
                lastUpdateTime = currentTime
            }
        }
        return result
    }

    private fun notifyProgress() {
        val percentage = if (totalSize > 0) (bytesRead.toFloat() / totalSize.toFloat()) * 100f else 0f
        progressCallback(bytesRead, totalSize, percentage)
    }

    override fun read(): Int {
        val byte = inputStream.read()
        if (byte != -1) {
            bytesRead++
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= updateIntervalMs) {
                notifyProgress()
                lastUpdateTime = currentTime
            }
        }
        return byte
    }

    override fun close() {
        // Send final progress update
        notifyProgress()
        inputStream.close()
    }

    // Delegate other methods...
    override fun available() = inputStream.available()
    override fun mark(readlimit: Int) = inputStream.mark(readlimit)
    override fun reset() = inputStream.reset()
    override fun markSupported() = inputStream.markSupported()
}