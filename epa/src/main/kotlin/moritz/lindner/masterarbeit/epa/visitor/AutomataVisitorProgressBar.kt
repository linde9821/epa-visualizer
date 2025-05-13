package moritz.lindner.masterarbeit.epa.visitor

import me.tongfei.progressbar.ConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle

// TODO: change structure to delegate to other so it can be used as the outer wrapper
class AutomataVisitorProgressBar<T : Comparable<T>>(
    private val visitor: AutomataVisitor<T>,
    private val taskName: String,
) : AutomataVisitor<T> by visitor {
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
