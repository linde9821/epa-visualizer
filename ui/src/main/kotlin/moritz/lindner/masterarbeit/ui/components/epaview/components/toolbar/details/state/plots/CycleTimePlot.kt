package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state.plots

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
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
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
) {
    val epaService = EpaService<Long>()
    val cycleTimes = epaService.computeCycleTimes(extendedPrefixAutomaton)

    val plot = createCycleTimeHistogram(
        cycleTimes = cycleTimes.cycleTimesOfState(state, Long::minus),
    )

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
