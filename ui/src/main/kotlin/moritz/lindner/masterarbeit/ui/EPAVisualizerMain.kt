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
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import moritz.lindner.masterarbeit.buildconfig.BuildConfig
import moritz.lindner.masterarbeit.ui.common.AboutPanel.showAboutDialog
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
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

val logger = KotlinLogging.logger {}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalLayoutApi
@ExperimentalJewelApi
fun main() {
    logger.info { "Starting EPA-Visualizer" }

    setSystemProperties()
    val backgroundDispatcher = buildDispatcherAndMonitoring()


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
}

private fun buildDispatcherAndMonitoring(): ExecutorCoroutineDispatcher {
    val i = AtomicInteger(0)
    val threadFactory = ThreadFactory { runnable ->
        Thread(runnable, "EPA-Visualizer-Background-Thread ${i.incrementAndGet()}")
    }
    val threads = Runtime.getRuntime().availableProcessors() / 2
    val executor = Executors.newFixedThreadPool(threads, threadFactory)
    val backgroundDispatcher = executor.asCoroutineDispatcher()

    val memoryMonitor = ScheduledThreadPoolExecutor(1)

    memoryMonitor.scheduleAtFixedRate({
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usagePercent = (usedMemory.toDouble() / maxMemory * 100).toInt()

        if (usagePercent > 70) {
            logger.warn { "High memory usage: $usagePercent%" }
        }
    }, 0, 30, TimeUnit.SECONDS)

    logger.info { "Starting background dispatcher with $threads threads" }

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "Application is shutting" }
        backgroundDispatcher.close()
        executor.shutdownNow()
        logger.info { "Shutdown complete" }
    })

    return backgroundDispatcher
}

private fun setSystemProperties() {
    // Cross-platform application name settings
    System.setProperty("apple.awt.application.name", APPLICATION_NAME) // macOS
    System.setProperty("awt.useSystemAAFontSettings", "on") // Better font rendering
    System.setProperty("swing.aatext", "true") // Anti-aliasing

    // Windows-specific properties
    System.setProperty("sun.awt.useSystemAAFontSettings", "on")
    System.setProperty(
        "swing.defaultlaf",
        System.getProperty("swing.defaultlaf", "javax.swing.plaf.nimbus.NimbusLookAndFeel")
    )

    // Linux/Unix-specific properties
    System.setProperty("awt.useSystemAAFontSettings", "lcd")
    System.setProperty("swing.aatext", "true")

    // General application properties that work across platforms
    System.setProperty("java.awt.headless", "false")
    System.setProperty("file.encoding", "UTF-8")
}