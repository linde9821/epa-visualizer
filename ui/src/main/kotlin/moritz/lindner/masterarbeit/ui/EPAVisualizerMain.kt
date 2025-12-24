package moritz.lindner.masterarbeit.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.buildconfig.BuildConfig
import moritz.lindner.masterarbeit.ui.common.AboutPanel.showAboutDialog
import moritz.lindner.masterarbeit.ui.common.Constants.APPLICATION_NAME
import moritz.lindner.masterarbeit.ui.common.Icons
import moritz.lindner.masterarbeit.ui.components.EPAVisualizerUi
import moritz.lindner.masterarbeit.ui.generated.resources.Res
import moritz.lindner.masterarbeit.ui.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.jewel.foundation.DisabledAppearanceValues
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.createDefaultTextStyle
import org.jetbrains.jewel.intui.standalone.theme.createEditorTextStyle
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.standalone.theme.light
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.standalone.window.Window
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import org.jetbrains.jewel.window.styling.TitleBarStyle
import org.jetbrains.skiko.SkikoProperties
import java.awt.Desktop
import java.net.URI
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

val logger = KotlinLogging.logger {}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalLayoutApi
@ExperimentalJewelApi
fun main() {
    try {
        logger.info { "Starting EPA-Visualizer" }
        setSystemProperties()
        setupMemoryMonitoring()
        runApplication()
    } catch (e: Exception) {
        logger.error(e) { "Failed to start application" }
        throw e
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTitleWithLogo() {
    Tooltip(
        tooltip = { Text("$APPLICATION_NAME version ${BuildConfig.APP_VERSION}") }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(APPLICATION_NAME)
            Icon(Icons.logo, "App Logo", modifier = Modifier.size(32.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GitHubButton() {
    Tooltip({ Text("Open the $APPLICATION_NAME repository on Github") }) {
        IconButton(
            onClick = {
                runCatching {
                    Desktop.getDesktop().browse(URI.create("https://github.com/linde9821/epa-visualizer"))
                }.onFailure { e ->
                    logger.error(e) { "Failed to open GitHub repository" }
                }
            },
            modifier = Modifier.size(40.dp).padding(5.dp),
        ) {
            Icon(Icons.gitHub, "Github")
        }
    }
}

private fun setupDesktopIntegration() {
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler { _ -> showAboutDialog() }
        }
    }
}

private fun runApplication() {
    application {
        logger.info { "Skiko rendering API: ${SkikoProperties.renderApi.name}" }

        val textStyle = JewelTheme.createDefaultTextStyle()
        val editorStyle = JewelTheme.createEditorTextStyle()
        val disabledAppearanceValues = DisabledAppearanceValues.light()

        val themeDefinition =
            JewelTheme.lightThemeDefinition(
                defaultTextStyle = textStyle,
                editorTextStyle = editorStyle,
                disabledAppearanceValues = disabledAppearanceValues,
            )

        IntUiTheme(
            theme = themeDefinition,
            styling = ComponentStyling.default().decoratedWindow(
                titleBarStyle = TitleBarStyle.light(),
            ),
            swingCompatMode = true
        ) {
            DecoratedWindow(
                onCloseRequest = ::exitApplication,
                state = WindowState(
                    placement = WindowPlacement.Maximized,
                    isMinimized = false,
                ),
                title = APPLICATION_NAME,
                icon = painterResource(Res.drawable.logo),
            ) {
                LaunchedEffect(Unit) {
                    setupDesktopIntegration()
                }

                TitleBar(Modifier.newFullscreenControls()) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppTitleWithLogo()
                    }

                    Row(Modifier.align(Alignment.End)) {
                        GitHubButton()
                    }

                }
                EPAVisualizerUi()
            }
        }
    }
}

private fun setupMemoryMonitoring() {
    val memoryMonitor = ScheduledThreadPoolExecutor(1) { runnable ->
        Thread(runnable, "Memory-Monitor").apply { isDaemon = true }
    }

    memoryMonitor.scheduleAtFixedRate({
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usagePercent = (usedMemory.toDouble() / maxMemory * 100).toInt()

        if (usagePercent > 80) {
            logger.warn { "High memory usage: $usagePercent% (${usedMemory / 1024 / 1024}MB / ${maxMemory / 1024 / 1024}MB)" }
            if (usagePercent > 90) {
                System.gc()
            }
        }
    }, 30, 30, TimeUnit.SECONDS)
}

private fun setSystemProperties() {
    val properties = mapOf(
        "apple.awt.application.name" to APPLICATION_NAME,
        "awt.useSystemAAFontSettings" to "on",
        "swing.aatext" to "true",
        "sun.awt.useSystemAAFontSettings" to "on",
        "java.awt.headless" to "false",
        "file.encoding" to "UTF-8"
    )

    properties.forEach { (key, value) ->
        System.setProperty(key, value)
    }

    logger.debug { "System properties configured" }
}