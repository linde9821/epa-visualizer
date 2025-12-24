package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.ui.common.Formatting.roundToLongSafe
import moritz.lindner.masterarbeit.ui.common.Formatting.toContextual
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state.plots.CumulativeEventsPlot
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state.plots.CycleTimePlot
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Chip
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
import java.text.DecimalFormat
import java.time.Duration
import kotlin.math.roundToLong


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StateInfo(
    selectedState: State,
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    onStateSelected: (State) -> Unit,
    locate: (State) -> Unit
) {
    val epaService = EpaService<Long>()
    val formatter = DecimalFormat("#,###")
    val stateName = selectedState.name
    val seq = extendedPrefixAutomaton.sequence(selectedState)
    val normalizedFrequency =
        epaService.getNormalizedStateFrequency(extendedPrefixAutomaton).frequencyByState(selectedState)
    val freqFormatted = "%.1f".format(normalizedFrequency * 100f)
    val partition = extendedPrefixAutomaton.partition(selectedState)
    val depth = epaService.getDepth(selectedState)
    val cycleTimes = epaService.computeCycleTimes(extendedPrefixAutomaton)
    val cycleTimesOfState = cycleTimes.averageCycleTimesOfState(selectedState, Long::minus)
    val outgoingTransitions = epaService.outgoingTransitions(extendedPrefixAutomaton, selectedState)
    val incomingTransitions = epaService.incomingTransitions(extendedPrefixAutomaton, selectedState)
    val traces = epaService.getTracesByState(extendedPrefixAutomaton, selectedState)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionContainer {
                Text(
                    text = stateName,
                    style = JewelTheme.typography.h3TextStyle,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            Tooltip(
                tooltip = {
                    Text("Click to jump to the state in the visualization.")
                },
                content = {
                    IconButton(
                        onClick = { locate(selectedState) },
                    ) {
                        Icon(
                            key = AllIconsKeys.General.Locate,
                            contentDescription = "Locate",
                        )
                    }
                }
            )
        }

        ClosableGroup(
            "Details"
        ) {
            if (selectedState is State.PrefixState) {
                InfoRow(label = "Activity", value = selectedState.via.name)
            }
            InfoRow(label = "Partition", value = formatter.format(partition))
            InfoRow(label = "Depth", value = formatter.format(depth))
            InfoRow(
                label = "Events",
                value = formatter.format(seq.size)
            )
            InfoRow(
                label = "Traces",
                value = formatter.format(traces.size),
            )
            InfoRow(
                label = "(Normalized) Frequency",
                value = "$freqFormatted%",
                hintText = """The percentage of traces seen by ${selectedState.name}, 
                    |compared to the total amount of traces in the whole EPA.""".trimMargin()
            )
            InfoRow(
                label = "Average State Cycle Time",
                value = Duration.ofMillis(cycleTimesOfState.average().roundToLongSafe()).toContextual(),
                hintText = "Average time it takes traces to get from this state to any next state. 0 if its a last state."
            )
        }

        ClosableGroup("Transitions") {
            if (incomingTransitions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Incoming (${incomingTransitions.size})",
                        style = JewelTheme.typography.regular,
                        fontWeight = FontWeight.Medium
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        incomingTransitions.forEach { transition ->
                            Chip(onClick = {
                                onStateSelected(transition.start)
                            }) {
                                Text(transition.start.name, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            if (outgoingTransitions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Outgoing (${outgoingTransitions.size})",
                        style = JewelTheme.typography.regular,
                        fontWeight = FontWeight.Medium
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        outgoingTransitions.forEach { transition ->
                            Chip(onClick = {
                                onStateSelected(transition.end)
                            }) {
                                Text(transition.end.name, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        ClosableGroup("Path from Root") {
            val pathToRoot = epaService.getPathFromRoot(selectedState)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp)
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(pathToRoot) { index, state ->
                    val count = if (index + 1 >= pathToRoot.size) {
                        0
                    } else {
                        val nextStates = pathToRoot[index + 1]
                        extendedPrefixAutomaton.sequence(nextStates).count()
                    }

                    Chip(
                        onClick = {
                            onStateSelected(state)
                        },
                        selected = state == selectedState
                    ) {
                        Text(state.name, fontSize = 11.sp)
                    }
                    if (index < pathToRoot.lastIndex) {
                        Text(
                            text = formatter.format(count),
                            fontSize = 9.sp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                        Text(
                            text = "↓",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }

        ClosableGroup(
            "Traces"
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp)
                    .padding(4.dp)
            ) {
                items(traces.toList()) { trace ->
                    TraceDetail(trace, selectedState, extendedPrefixAutomaton) {
                        onStateSelected(it)
                    }
                }
            }
        }

        ClosableGroup(
            "Cumulative Events"
        ) {
            CumulativeEventsPlot(sequence = seq)
        }

        ClosableGroup(
            "Cycle Time Histogram"
        ) {
            CycleTimePlot(
                state = selectedState,
                cycleTimesOfState = cycleTimesOfState
            )
        }
    }
}