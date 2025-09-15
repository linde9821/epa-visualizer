package moritz.lindner.masterarbeit.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.asCoroutineDispatcher
import moritz.lindner.masterarbeit.buildconfig.BuildConfig
import moritz.lindner.masterarbeit.ui.common.Constants.APPLICATION_NAME
import moritz.lindner.masterarbeit.ui.common.Icons
import moritz.lindner.masterarbeit.ui.components.EPAVisualizerUi
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.createDefaultTextStyle
import org.jetbrains.jewel.intui.standalone.theme.createEditorTextStyle
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.intui.window.styling.lightWithLightHeader
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import org.jetbrains.jewel.window.styling.TitleBarStyle
import org.jetbrains.skiko.SkikoProperties
import java.awt.Desktop
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JOptionPane
import javax.swing.JOptionPane.showMessageDialog

val logger = KotlinLogging.logger {}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalLayoutApi
@ExperimentalJewelApi
fun main() {
    logger.info { "Starting EPA-Visualizer" }
    val i = AtomicInteger(0)

    val threadFactory =
        ThreadFactory { runnable ->
            Thread(runnable, "EPA-Visualizer-Background-Thread ${i.incrementAndGet()}")
        }
    val executor = Executors.newFixedThreadPool(4, threadFactory)
    val backgroundDispatcher = executor.asCoroutineDispatcher()
    application {
        logger.info { "Skiko rendering API: ${SkikoProperties.renderApi.name}" }

        val textStyle = JewelTheme.createDefaultTextStyle()
        val editorStyle = JewelTheme.createEditorTextStyle()

        val themeDefinition =
            JewelTheme.lightThemeDefinition(defaultTextStyle = textStyle, editorTextStyle = editorStyle)

        IntUiTheme(
            theme = themeDefinition,
            styling = ComponentStyling.default().decoratedWindow(
                titleBarStyle = TitleBarStyle.lightWithLightHeader(),
                windowStyle = DecoratedWindowStyle.light()
            ),
        ) {
            DecoratedWindow(
                onCloseRequest = ::exitApplication,
                state = WindowState(
                    placement = WindowPlacement.Floating,
                    isMinimized = false,
                    size = DpSize(800.dp, 800.dp)
                ),
                title = APPLICATION_NAME,
                icon = painterResource("icons/logo.png"),
            ) {

                LaunchedEffect(Unit) {
                    if (Desktop.isDesktopSupported()) {
                        val desktop = Desktop.getDesktop()
                        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                            desktop.setAboutHandler { _ ->
                                showAboutDialog()
                            }
                        }
                    }
                }

                TitleBar(Modifier.newFullscreenControls()) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Tooltip(
                            tooltip = { Text("$APPLICATION_NAME version ${BuildConfig.APP_VERSION}") }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(APPLICATION_NAME)
                                Icon(Icons.logo, "App Logo", modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    Row(Modifier.align(Alignment.End)) {
                        Tooltip({ Text("Open the $APPLICATION_NAME repository on Github") }) {
                            IconButton(
                                {
                                    Desktop.getDesktop()
                                        .browse(URI.create("https://github.com/linde9821/epa-visualizer"))
                                },
                                Modifier.size(40.dp).padding(5.dp),
                            ) {
                                Icon(Icons.gitHub, "Github")
                            }
                        }
                    }

                }
                EPAVisualizerUi(backgroundDispatcher)
            }
        }
    }
    executor.shutdownNow()
}

private fun showAboutDialog() {
    showMessageDialog(
        null,
        """
        $APPLICATION_NAME
        Version: ${BuildConfig.APP_VERSION}
        
        Interactive Visualization of Extended Prefix Automaton Using Radial Trees
        
        A tool for analyzing trace variants in large, complex event logs using 
        Extended Prefix Automata (EPA) and radial tidy tree layouts. This 
        visualization approach encodes thousands of trace variants while 
        minimizing visual clutter and supporting interactive filtering.
        
        Built as part of a Master's Thesis and Study Project (Studienarbeit) at 
        Humboldt-Universität zu Berlin.
        
        This application and the master thesis is currently under development and not finished.
        
        Built with Kotlin and Compose Desktop
        Process Mining • Event Log Visualization • Variant Analysis
        
        Author: Moritz Lindner
        Supervisor: Prof. Dr. Jan Mendling
        
        GitHub: https://github.com/linde9821/epa-visualizer
        
        © 2025 Moritz Lindner
        """.trimIndent(),
        "About $APPLICATION_NAME",
        JOptionPane.INFORMATION_MESSAGE
    )
}