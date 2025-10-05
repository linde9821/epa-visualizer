package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state.plots

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.ui.common.BinCalculator
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot

@Composable
fun CycleTimePlot(
    state: State,
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    modifier: Modifier = Modifier.Companion
) {
    val sequence = extendedPrefixAutomaton.sequence(state)
    if (sequence.isEmpty()) {
        Text(
            text = "No events to display",
            style = JewelTheme.Companion.typography.regular,
            modifier = modifier.padding(16.dp)
        )
        return
    }

    val epaService = EpaService<Long>()
    val cycleTimes = epaService.computeCycleTimes(extendedPrefixAutomaton, state, Long::minus)

    val plot = createCycleTimeHistogram(
        cycleTimes = cycleTimes,
    )

    PlotPanel(
        figure = plot,
        modifier = Modifier.Companion
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
    title: String = "Distribution of Cycle Times",
    xLabel: String = "Cycle Time (seconds)",
    yLabel: String = "Frequency"
): Plot {
    val bins = BinCalculator.autoBins(cycleTimes)
    val data = mapOf("cycleTime" to cycleTimes)

    val plot = letsPlot(data) +
            geomHistogram(
                bins = bins,
                fill = "#4A90E2",
                color = "#2E5C8A",
                alpha = 0.7
            ) {
                x = "cycleTime"
            } +
            ggtitle(title) +
            labs(x = xLabel, y = yLabel)

    return plot
}
