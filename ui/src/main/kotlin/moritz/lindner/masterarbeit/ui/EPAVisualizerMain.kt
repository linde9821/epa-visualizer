package moritz.lindner.masterarbeit.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import moritz.lindner.masterarbeit.ui.components.EPAVisualizerUi

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
            EPAVisualizerUi()
        }
    }
