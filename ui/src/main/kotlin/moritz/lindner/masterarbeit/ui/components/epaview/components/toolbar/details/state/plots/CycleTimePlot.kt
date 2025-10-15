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
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.cycletime.CycleTimes
import moritz.lindner.masterarbeit.ui.common.BinCalculator
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import java.time.Duration

@Composable
fun CycleTimePlot(
    state: State,
    cycleTimes: CycleTimes<Long>,
    modifier: Modifier = Modifier.Companion
) {
    val cycleTimeOfState = cycleTimes.cycleTimesOfState(state, Long::minus)

    val plot = createCycleTimeHistogram(
        cycleTimes = cycleTimeOfState,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Min", value = Duration.ofMillis(cycleTimeOfState.minOrNull() ?: 0).toString())
            StatItem(label = "Average", value = Duration.ofMillis(cycleTimeOfState.average().toLong()).toString())
            StatItem(label = "Max", value = Duration.ofMillis(cycleTimeOfState.maxOrNull() ?: 0).toString())
        }
    }
}

private fun createCycleTimeHistogram(
    cycleTimes: List<Long>,
    xLabel: String = "Cycle Time (in Hours)",
    yLabel: String = "Frequency"
): Plot {
    val bins = BinCalculator.autoBins(cycleTimes)
    val data = mapOf("cycleTime" to cycleTimes.map { Duration.ofMillis(it).toHours() })

    val plot = letsPlot(data) +
            geomHistogram(
                bins = bins,
                fill = "#4A90E2",
                color = "#2E5C8A",
                alpha = 0.7
            ) {
                x = "cycleTime"
            } + labs(x = xLabel, y = yLabel)

    return plot
}
