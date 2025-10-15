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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state.plots.CumulativeEventsPlot
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state.plots.CycleTimePlot
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state.plots.TimeToReachPlot
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Chip
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
import java.time.Duration


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StateInfo(
    selectedState: State,
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    onStateSelected: (State) -> Unit,
    locate: (State) -> Unit
) {
    val epaService = EpaService<Long>()

    val stateName = selectedState.name
    val seq = extendedPrefixAutomaton.sequence(selectedState)
    val normalizedFrequency =
        epaService.getNormalizedStateFrequency(extendedPrefixAutomaton).frequencyByState(selectedState)
    val freqFormatted = "%.1f".format(normalizedFrequency * 100f)
    val partition = extendedPrefixAutomaton.partition(selectedState)
    val depth = epaService.getDepth(selectedState)
    val cycleTime = epaService.computeCycleTimes(extendedPrefixAutomaton).cycleTimesOfState(selectedState, Long::minus)
        .let { times ->
            if (times.isEmpty()) {
                0f
            } else Duration.ofMillis(times.average().toLong())
        }
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

            IconButton(
                onClick = { locate(selectedState) },
            ) {
                Icon(
                    key = AllIconsKeys.General.Locate,
                    contentDescription = "Locate",
                )
            }
        }

        // Metrics Section
        ClosableGroup(
            "Details"
        ) {
            if (selectedState is State.PrefixState) {
                InfoRow(label = "Activity", value = selectedState.via.name)
            }
            InfoRow(label = "Partition", value = partition.toString())
            InfoRow(label = "Depth", value = depth.toString())
            InfoRow(
                label = "Events",
                value = seq.size.toString()
            )
            InfoRow(
                label = "Traces",
                value = traces.size.toString(),
            )
            InfoRow(
                label = "(Normalized) Frequency",
                value = "$freqFormatted%",
                hintText = """The percentage of traces seen by ${selectedState.name}, 
                    |compared to the total amount of traces in the whole EPA.""".trimMargin()
            )
            InfoRow(
                label = "Cycle Time",
                value = cycleTime.toString(),
                hintText = "Average time it takes traces to get from this state to a next"
            )
        }

        // Transitions Section
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
                    Chip(onClick = {
                        onStateSelected(state)
                    }) {
                        Text(state.name, fontSize = 11.sp)
                    }

                    if (index < pathToRoot.lastIndex) {
                        Text(
                            text = "↓",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }

        // Traces
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
            "Time to reach state from root"
        ) {
            TimeToReachPlot(
                state = selectedState,
                extendedPrefixAutomaton = extendedPrefixAutomaton,
            )
        }

        ClosableGroup(
            "Cycle Time Histogram"
        ) {
            CycleTimePlot(
                state = selectedState,
                extendedPrefixAutomaton = extendedPrefixAutomaton,
            )
        }
    }
}