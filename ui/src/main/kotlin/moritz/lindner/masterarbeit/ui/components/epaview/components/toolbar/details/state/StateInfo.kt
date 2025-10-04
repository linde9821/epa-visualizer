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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.details.state.plots.TimeToReachPlot
import org.jetbrains.jewel.foundation.Stroke
import org.jetbrains.jewel.foundation.modifier.border
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Chip
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.typography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StateInfo(
    selectedState: State,
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    onStateSelected: (State) -> Unit
) {
    val epaService = EpaService<Long>()

    val stateName = selectedState.name
    val seq = extendedPrefixAutomaton.sequence(selectedState)
    val partition = extendedPrefixAutomaton.partition(selectedState)
    val depth = epaService.getDepth(selectedState)
    val outgoingTransitions = epaService.outgoingTransitions(extendedPrefixAutomaton, selectedState)
    val incomingTransitions = epaService.incomingTransitions(extendedPrefixAutomaton, selectedState)
    val isLeaf = outgoingTransitions.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "State Information",
                style = JewelTheme.typography.small
            )
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

                if (isLeaf) {
                    Chip(onClick = {}) {
                        Text("Leaf", fontSize = 11.sp)
                    }
                }
            }
        }

        Divider(orientation = Orientation.Horizontal)

        // Metrics Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    alignment = Stroke.Alignment.Inside,
                    width = 1.dp,
                    color = JewelTheme.globalColors.borders.normal,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InfoRow(label = "Partition", value = partition.toString())
            InfoRow(label = "Depth", value = depth.toString())
            InfoRow(label = "Events", value = seq.size.toString())
        }

        // Transitions Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Transitions",
                style = JewelTheme.typography.h4TextStyle,
                fontWeight = FontWeight.SemiBold
            )

            if (incomingTransitions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Incoming (${incomingTransitions.size}",
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
                        text = "Outgoing (${outgoingTransitions.size}",
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
                                Tooltip(
                                    tooltip = {
                                        Text("via: ${transition.activity.name}")
                                    }
                                ) {
                                    Text(transition.end.name, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Divider(orientation = Orientation.Horizontal)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Path from Root",
                style = JewelTheme.typography.h4TextStyle,
                fontWeight = FontWeight.SemiBold
            )

            val pathToRoot = epaService.getPathFromRoot(selectedState)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp)
                    .padding(4.dp)
            ) {
                itemsIndexed(pathToRoot) { index, state ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Chip(onClick = {
                            onStateSelected(state)
                        }) {
                            Text(state.name, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Divider(orientation = Orientation.Horizontal)

        CumulativeEventsPlot(sequence = seq)

        Divider(orientation = Orientation.Horizontal)

        TimeToReachPlot(
            state = selectedState,
            extendedPrefixAutomaton = extendedPrefixAutomaton,
        )
    }
}
