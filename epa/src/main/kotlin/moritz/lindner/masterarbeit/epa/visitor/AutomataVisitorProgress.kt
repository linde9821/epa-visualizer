package moritz.lindner.masterarbeit.epa.visitor

import io.github.oshai.kotlinlogging.KotlinLogging
import me.tongfei.progressbar.ConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle

abstract class AutomataVisitorProgress<T : Comparable<T>>(
    private val taskName: String,
) : AutomataVisitor<T> {
    private val logger = KotlinLogging.logger { }

    private var progressBar: ProgressBar =
        ProgressBarBuilder()
            .apply {
                showSpeed()
                setConsumer(ConsoleProgressBarConsumer(System.out))
                setTaskName(taskName)
                setUnit(" States", 1L)
                setMaxRenderedLength(80)
                setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
            }.build()

    override fun onProgress(
        current: Long,
        total: Long,
    ) {
        progressBar.maxHint(total)
        progressBar.stepTo(current)
    }
}
