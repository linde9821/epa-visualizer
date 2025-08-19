package moritz.lindner.masterarbeit.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import moritz.lindner.masterarbeit.ui.Constants.applicationName
import moritz.lindner.masterarbeit.ui.components.EPAVisualizerUi
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.foundation.util.JewelLogger
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
import org.jetbrains.jewel.ui.icon.PathIconKey
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

val logger = KotlinLogging.logger {}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalLayoutApi
@ExperimentalJewelApi
fun main() {
    JewelLogger.getInstance("EPA-Visualizer").info("Starting EPA-Visualizer")
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
                title = applicationName,
                icon = painterResource("icons/logo.png"),
            ) {
                TitleBar(Modifier.newFullscreenControls()) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(applicationName)
                        Icon(Icons.logo, "App Logo", modifier = Modifier.size(32.dp))
                    }

                    Row(Modifier.align(Alignment.End)) {
                        Tooltip({ Text("Open the $applicationName repository on Github") }) {
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

object Icons {
    val gitHub: PathIconKey = PathIconKey("icons/github.svg", Icons::class.java)
    val logo: PathIconKey = PathIconKey("icons/logo.png", Icons::class.java)
}

object Constants {
    val applicationName = "EPA Visualizer"
}