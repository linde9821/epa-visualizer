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
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomBoxplot
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.label.xlab
import org.jetbrains.letsPlot.label.ylab
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.themes.elementText
import org.jetbrains.letsPlot.themes.theme
import java.time.Duration

@Composable
fun TimeToReachPlot(
    state: State,
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    modifier: Modifier = Modifier.Companion
) {
    val sequence = extendedPrefixAutomaton.sequence(state)

    val epaService = EpaService<Long>()
    val traces = epaService.getTracesByState(extendedPrefixAutomaton, state)

    val timeToReachData = traces.map { trace ->
        val first = trace.first()
        val atState = trace.first { event -> event in sequence }

        val durationMs = atState.timestamp - first.timestamp
        val durationMinutes = durationMs / (1000.0 * 60.0)

        mapOf(
            "event" to "State",
            "duration" to durationMinutes
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Create plot data
        val data = mapOf(
            "event" to timeToReachData.map { it["event"] as String },
            "duration" to timeToReachData.map { it["duration"] as Double }
        )

        // Create the box plot
        val plot = letsPlot(data) {
            x = "event"
            y = "duration"
        } +
                geomBoxplot(color = "#2196F3", fill = "#E3F2FD") +
                xlab("") +
                ylab("Duration (minutes)") +
                ggsize(800, 300) +
                theme(
                    axisTextX = elementText(angle = 0)
                ).legendPositionNone()

        PlotPanel(
            figure = plot,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            computationMessagesHandler = { messages ->
                messages.forEach { message ->
                    logger.debug { "Plot computation message: $message" }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        val durations = timeToReachData.map { it["duration"] as Double }
        val minDuration = Duration.ofMillis(durations.minOrNull()?.toLong() ?: 0L)
        val maxDuration = Duration.ofMillis(durations.maxOrNull()?.toLong() ?: 0L)
        val avgDuration = Duration.ofMillis(durations.average().toLong())

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Min", value = minDuration.toString())
            StatItem(label = "Average", value = avgDuration.toString())
            StatItem(label = "Max", value = maxDuration.toString())
        }
    }
}