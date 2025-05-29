package moritz.lindner.masterarbeit.ui.components.treeview.components.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.filter.PartitionFrequencyFilter
import moritz.lindner.masterarbeit.epa.visitor.statistics.NormalizedPartitionFrequencyVisitor
import kotlin.math.max

@Composable
fun PartitionFrequencyFilterUi(
    epa: ExtendedPrefixAutomata<Long>,
    onFilter: (EpaFilter<Long>) -> Unit,
) {
    var sliderValue by remember { mutableStateOf(0.0f) }
    var threshold by remember { mutableStateOf(0.0f) }

    Column {
        // TODO: run in background
        val frequencyPartitionVisitor = NormalizedPartitionFrequencyVisitor<Long>()
        epa.copy().acceptDepthFirst(frequencyPartitionVisitor)

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
            items(epa.getAllPartitions().sorted()) {
                Text("$it: ${frequencyPartitionVisitor.frequencyByPartition(it)}")
            }
        }
    }
}
