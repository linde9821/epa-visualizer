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
import moritz.lindner.masterarbeit.epa.filter.StateFrequencyFilter
import moritz.lindner.masterarbeit.epa.visitor.statistics.NormalizedStateFrequencyVisitor
import kotlin.math.max

@Composable
fun StateFrequencyFilterUi(
    epa: ExtendedPrefixAutomata<Long>,
    onFilter: (EpaFilter<Long>) -> Unit,
) {
    var sliderValue by remember { mutableStateOf(0.0f) }
    var threshold by remember { mutableStateOf(0.0f) }

    Column {
        // TODO: run in background
        val frequencyStateVisitor = NormalizedStateFrequencyVisitor<Long>()
        epa.copy().acceptDepthFirst(frequencyStateVisitor)

        val rawMinFreq = frequencyStateVisitor.min()
        val rawMaxFreq = frequencyStateVisitor.max() + 0.1f

        val minFreq = max(rawMinFreq, 1e-6f)
        val maxFreq = max(rawMaxFreq, minFreq * 10f)

        Text("min=$minFreq, max=$maxFreq, slider=$sliderValue, threshold=${"%.4f".format(threshold)}")

        Slider(
            value = sliderValue,
            onValueChange = { value ->
                sliderValue = value
                threshold = sliderToThreshold(sliderValue, minFreq, maxFreq)
                onFilter(
                    StateFrequencyFilter(threshold),
                )
            },
            valueRange = 0f..1f,
        )
        LazyColumn {
            items(epa.states.toList()) { state ->
                Text("${state.name}: ${frequencyStateVisitor.frequencyByState(state)}")
            }
        }
    }
}
