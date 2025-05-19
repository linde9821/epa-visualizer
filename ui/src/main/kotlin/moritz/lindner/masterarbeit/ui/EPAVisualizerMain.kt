package moritz.lindner.masterarbeit.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.ui.components.EPAVisualizerUi
import org.jetbrains.skiko.SkikoProperties

val logger = KotlinLogging.logger {}

fun main() =

    application {
        Window(
            onCloseRequest = ::exitApplication,
            state =
                WindowState(
                    placement = WindowPlacement.Maximized,
                    isMinimized = false,
                ),
            title = "EPA Visualizer",
        ) {
            logger.info { "Skiko rendering API: ${SkikoProperties.renderApi.name}" }
            EPAVisualizerUi()
        }
    }
