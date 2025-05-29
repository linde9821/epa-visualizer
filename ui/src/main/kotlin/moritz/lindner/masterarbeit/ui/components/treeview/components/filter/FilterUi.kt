package moritz.lindner.masterarbeit.ui.components.treeview.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Slider
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.filter.ActivityFilter
import moritz.lindner.masterarbeit.epa.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.filter.PartitionFrequencyFilter
import moritz.lindner.masterarbeit.epa.visitor.statistics.NormalizedPartitionFrequencyVisitor

@Composable
fun FilterUi(
    epa: ExtendedPrefixAutomata<Long>,
    onApply: (EpaFilter<Long>) -> Unit,
) {
    val activities =
        remember {
            mutableStateListOf(*epa.activities.map { it to true }.toTypedArray())
        }

    val tabs = listOf("Activity", "State Frequency", "Partition Frequency", "Chain Pruning")
    var selectedIndex by remember { mutableStateOf(0) }
    var stateFrequencyThreashold by remember { mutableStateOf(100f) }
    var partitionFrequencyThreashold by remember { mutableStateOf(100f) }

    Column(
        modifier =
            Modifier.Companion
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                shape = RoundedCornerShape(24.dp),
                onClick = {
                    val selectedActivities = activities.filter { it.second }.map { it.first }
                    val activityFilter = ActivityFilter<Long>(selectedActivities.toHashSet())

                    val combined = PartitionFrequencyFilter<Long>(partitionFrequencyThreashold / 100f)
//                        activityFilter
//                            .then(
//                                StateFrequencyFilter(stateFrequencyThreashold / 100f),
//                            ).then(
//                                PartitionFrequencyFilter(partitionFrequencyThreashold / 100f),
//                            )

                    onApply(combined)
                },
                modifier = Modifier.Companion.height(48.dp),
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Abort")
                Spacer(Modifier.Companion.width(8.dp))
                Text("Apply", color = Color.Companion.White, style = MaterialTheme.typography.button)
            }
        }

        Spacer(modifier = Modifier.Companion.height(8.dp))

        ScrollableTabRow(selectedTabIndex = selectedIndex, backgroundColor = Color.Companion.White) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    text = { Text(title) },
                )
            }
        }

        Spacer(modifier = Modifier.Companion.height(8.dp))

        when (selectedIndex) {
            0 -> ActivityFilterTab(activities)
            1 -> {
                Column {
                    // State Frequency
                    Text("Frequency $stateFrequencyThreashold")
                    Slider(
                        value = stateFrequencyThreashold,
                        onValueChange = { stateFrequencyThreashold = it },
                        valueRange = 0.0f..100f,
                    )
                    LazyColumn {
                        items(epa.states.toList()) {
//                            Text("${it.name}: ${frequencyStateVisitor.frequencyByState(it)}")
                        }
                    }
                }
            }

            2 -> {
                Column {
                    // State Frequency

                    val frequencyParitionVisitor = NormalizedPartitionFrequencyVisitor<Long>()
                    epa.copy().acceptDepthFirst(frequencyParitionVisitor)

                    Text("Frequency $partitionFrequencyThreashold")
                    Slider(
                        value = partitionFrequencyThreashold,
                        onValueChange = { partitionFrequencyThreashold = it },
                        valueRange = 0.0f..100f,
                    )
                    LazyColumn {
                        items(epa.getAllPartitions().sorted()) {
                            Text("$it: ${frequencyParitionVisitor.frequencyByPartition(it)}")
                        }
                    }
                }
            }

            else -> {
                Text("TODO: implement ${tabs[selectedIndex]}")
            }
        }
    }
}
