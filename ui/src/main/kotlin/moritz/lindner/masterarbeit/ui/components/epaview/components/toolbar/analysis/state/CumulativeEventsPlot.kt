package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.analysis.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.ui.common.Formatting.asFormattedLocalDateTime
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
                if (messages.isNotEmpty()) {
                    logger.debug { "Plot computation messages: ${messages.joinToString(", ")}" }
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

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = JewelTheme.typography.h3TextStyle,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = JewelTheme.typography.small
        )
    }
}

private fun formatTimeSpan(start: Long, end: Long): String {
    val diff = end - start
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}