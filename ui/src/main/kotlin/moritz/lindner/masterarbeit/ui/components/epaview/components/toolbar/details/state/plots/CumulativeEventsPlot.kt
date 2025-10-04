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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.ui.common.Formatting.formatTimeSpan
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.label.xlab
import org.jetbrains.letsPlot.label.ylab
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleXDateTime
import org.jetbrains.letsPlot.themes.elementText
import org.jetbrains.letsPlot.themes.theme

@Composable
fun CumulativeEventsPlot(
    sequence: Set<Event<Long>>,
    modifier: Modifier = Modifier
) {
    // Early return if no data
    if (sequence.isEmpty()) {
        Text(
            text = "No events to display",
            style = JewelTheme.typography.regular,
            modifier = modifier.padding(16.dp)
        )
        return
    }

    val sortedEvents = sequence.sortedBy { it.timestamp }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Cumulative Events Over Time",
            style = JewelTheme.typography.h4TextStyle,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Create plot data - use immutable map
        val data = mapOf(
            "timestamp" to sortedEvents.map { it.timestamp },
            "cumulative" to sortedEvents.indices.map { it + 1 }
        )


        // Create the plot
        val plot = letsPlot(data) {
            x = "timestamp"
            y = "cumulative"
        } +
                geomLine(size = 2.0, color = "#2196F3") +
                scaleXDateTime(format = "%Y-%m-%d") +
                xlab("Time") +
                ylab("Cumulative Count") +
                ggsize(800, 400) +
                theme(
                    axisTextX = elementText(angle = 45, hjust = 1.0, vjust = 1.0)
                ).legendPositionNone()

        PlotPanel(
            figure = plot,
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
            StatItem(label = "Total Events", value = sortedEvents.size.toString())
            StatItem(
                label = "Time Span",
                value = formatTimeSpan(
                    sortedEvents.first().timestamp,
                    sortedEvents.last().timestamp
                )
            )
        }
    }
}

