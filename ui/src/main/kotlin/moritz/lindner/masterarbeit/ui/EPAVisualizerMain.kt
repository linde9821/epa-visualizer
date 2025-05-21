package moritz.lindner.masterarbeit.ui

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.asCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.EPAVisualizerUi
import org.jetbrains.skiko.SkikoProperties
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

val logger = KotlinLogging.logger {}

fun main() {
    val threadFactory =
        ThreadFactory { runnable ->
            Thread(runnable, "EPA-Visualizer-Background-Thread").apply {}
        }
    val executor = Executors.newSingleThreadExecutor(threadFactory)
    val backgroundDispatcher = executor.asCoroutineDispatcher()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            state =
                WindowState(
                    placement = WindowPlacement.Maximized,
                    isMinimized = false,
                ),
            title = "EPA Visualizer",
            icon = painterResource("logo.png"),
        ) {
            logger.info { "Skiko rendering API: ${SkikoProperties.renderApi.name}" }
            EPAVisualizerUi(backgroundDispatcher)
        }
    }
    executor.shutdownNow()
}
