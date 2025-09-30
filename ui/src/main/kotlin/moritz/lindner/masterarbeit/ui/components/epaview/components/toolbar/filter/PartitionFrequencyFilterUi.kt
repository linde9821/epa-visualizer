package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.filter

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.features.filter.CompressionFilter
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.filter.PartitionFrequencyFilter
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequency
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChainPruningFilterUi(onFilter: (EpaFilter<Long>) -> Unit) {

    var isChecked by remember { mutableStateOf(false) }

    Tooltip({
        Text("Depending on the size of the event log the chain compression might take some time to compute.")
    }) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Chain Pruning: ")
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = !isChecked
                    onFilter(CompressionFilter())
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PartitionFrequencyFilterUi(
    epa: ExtendedPrefixAutomaton<Long>,
    dispatcher: CoroutineDispatcher,
    onFilter: (EpaFilter<Long>) -> Unit,
) {

    val epaService = EpaService<Long>()

    var sliderValue by remember(epa) { mutableFloatStateOf(0.0f) }
    var threshold by remember(epa) { mutableFloatStateOf(0.0f) }
    var isLoading by remember { mutableStateOf(true) }
    var normalizedPartitionFrequency: NormalizedPartitionFrequency? by remember(epa) { mutableStateOf(null) }

    LaunchedEffect(epa) {
        isLoading = true
        withContext(dispatcher) {
            logger.info { "building partition filter" }
            normalizedPartitionFrequency = epaService.getNormalizedPartitionFrequency(epa)
        }
        isLoading = false
    }

    Column {
        if (!isLoading) {
            val rawMinFreq = normalizedPartitionFrequency!!.min()
            val rawMaxFreq = normalizedPartitionFrequency!!.max() + 0.1f

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
            Tooltip({
                Text("This shows only what would happen if the filter is applied to the current epa. If filters are applied before this the effects of this filter change.")
            }) {
                LazyColumn {
                    val partitions = normalizedPartitionFrequency!!.getPartitionsSortedByFrequencyDescending()
                    items(partitions) { partition ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val frequency = normalizedPartitionFrequency!!.frequencyByPartition(partition)
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

            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
            )
        }
    }
}
