package moritz.lindner.masterarbeit.ui.components.epaview.components.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.filter.PartitionFrequencyFilter
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequencyVisitor
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import kotlin.math.max

@Composable
fun PartitionFrequencyFilterUi(
    epa: ExtendedPrefixAutomaton<Long>,
    dispatcher: CoroutineDispatcher,
    onFilter: (EpaFilter<Long>) -> Unit,
) {
    var sliderValue by remember(epa) { mutableStateOf(0.0f) }
    var threshold by remember(epa) { mutableStateOf(0.0f) }
    var isLoading by remember { mutableStateOf(true) }
    val frequencyPartitionVisitor by remember(epa) { mutableStateOf(NormalizedPartitionFrequencyVisitor<Long>()) }

    LaunchedEffect(epa) {
        isLoading = true
        withContext(dispatcher) {
            logger.info { "building partition filter" }
            epa.copy().acceptDepthFirst(frequencyPartitionVisitor)
        }
        isLoading = false
    }

    Column {
        if (!isLoading) {
            val rawMinFreq = frequencyPartitionVisitor.min()
            val rawMaxFreq = frequencyPartitionVisitor.max() + 0.1f

            val minFreq = max(rawMinFreq, 1e-6f)
            val maxFreq = max(rawMaxFreq, minFreq * 10f)

            Column {
                Text("min=$minFreq")
                Text("max=$maxFreq")
                Text("threshold=${"%.4f".format(threshold)}")
            }


            Slider(
                value = sliderValue,
                onValueChange = { value ->
                    sliderValue = value
                    threshold = sliderToThreshold(sliderValue, minFreq, maxFreq)
                    onFilter(
                        PartitionFrequencyFilter(threshold),
                    )
                },
                valueRange = 0f..1f,
            )

            LazyColumn {
                val partitions = epa.getAllPartitions()
                    .sortedByDescending { frequencyPartitionVisitor.frequencyByPartition(it) }
                items(partitions) { partition ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val frequency = frequencyPartitionVisitor.frequencyByPartition(partition)
                        if (frequency < threshold) {
                            Icon(
                                key = AllIconsKeys.General.Note,
                                contentDescription = "Below threshold",
                            )
                        }
                        Text("Partition $partition:")
                        Spacer(modifier = Modifier.weight(1f))

                        Text("${"%.4f".format(frequency)}%")
                    }
                }
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
            )
        }
    }
}
