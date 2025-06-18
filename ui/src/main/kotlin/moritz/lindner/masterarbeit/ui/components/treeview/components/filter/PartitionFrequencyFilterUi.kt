package moritz.lindner.masterarbeit.ui.components.treeview.components.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.filter.PartitionFrequencyFilter
import moritz.lindner.masterarbeit.epa.visitor.statistics.NormalizedPartitionFrequencyVisitor
import moritz.lindner.masterarbeit.ui.logger
import kotlin.math.max

@Composable
fun PartitionFrequencyFilterUi(
    epa: ExtendedPrefixAutomata<Long>,
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

            Text("min=$minFreq, max=$maxFreq, slider=$sliderValue, threshold=${"%.4f".format(threshold)}")

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
                items(epa.getAllPartitions().sorted()) { partition ->
                    Text("Partition $partition: ${"%.4f".format(frequencyPartitionVisitor.frequencyByPartition(partition))}%")
                }
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
                strokeWidth = 6.dp,
                color = MaterialTheme.colors.primary,
            )
        }
    }
}
