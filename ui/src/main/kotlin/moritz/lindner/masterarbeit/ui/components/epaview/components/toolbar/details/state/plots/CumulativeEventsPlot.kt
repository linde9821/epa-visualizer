package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state.plots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.intern.layer.StatOptions
import org.jetbrains.letsPlot.label.xlab
import org.jetbrains.letsPlot.label.ylab
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleXDateTime
import org.jetbrains.letsPlot.themes.elementText
import org.jetbrains.letsPlot.themes.theme
import java.time.Duration

@Composable
fun CumulativeEventsPlot(
    sequence: Set<Event<Long>>,
    modifier: Modifier = Modifier
) {
    val sortedEvents = sequence.sortedBy { it.timestamp }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val data = mapOf(
            "timestamp" to sortedEvents.map { it.timestamp },
            "cumulative" to sortedEvents.indices.map { it + 1 }
        )

        val plotSpec = letsPlot(data) {
            x = "timestamp"
            y = "cumulative"
        } +
                geomLine(size = 2.0, color = "#2196F3", ) +
                scaleXDateTime(format = "%Y-%m-%d") +
                xlab("occurrence of event") +
                ylab("Cumulative count of events at state") +
                ggsize(800, 400) +
                theme(
                    axisTextX = elementText(angle = 45, hjust = 1.0, vjust = 1.0)
                ).legendPositionNone()

        PlotPanel(
            figure = plotSpec,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            computationMessagesHandler = { messages ->
                messages.forEach { message ->
                    logger.debug { "Plot computation message: $message" }
                }
            }
        )

        // Add summary statistics
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "total events at state", value = sortedEvents.size.toString())
            StatItem(
                label = "total timespan",
                value = Duration.ofMillis(
                    (sortedEvents.lastOrNull()?.timestamp ?: 0L) - (sortedEvents.firstOrNull()?.timestamp ?: 0L)
                ).toContextual()
            )
        }
    }
}

