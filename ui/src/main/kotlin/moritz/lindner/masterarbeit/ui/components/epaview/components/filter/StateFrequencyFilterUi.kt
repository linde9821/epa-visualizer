package moritz.lindner.masterarbeit.ui.components.epaview.components.filter

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
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.filter.StateFrequencyFilter
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedStateFrequency
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StateFrequencyFilterUi(
    epa: ExtendedPrefixAutomaton<Long>,
    dispatcher: CoroutineDispatcher,
    onFilter: (EpaFilter<Long>) -> Unit,
) {

    val epaService = EpaService<Long>()

    var sliderValue by remember { mutableStateOf(0.0f) }
    var threshold by remember { mutableStateOf(0.0f) }
    var isLoading by remember { mutableStateOf(true) }
    var normalizedStateFrequency: NormalizedStateFrequency? by remember(epa) {
        mutableStateOf(null)
    }

    LaunchedEffect(epa) {
        isLoading = true
        withContext(dispatcher) {
            logger.info { "building state filter" }
            normalizedStateFrequency = epaService.getNormalizedStateFrequency(epa)
        }
        isLoading = false
    }

    Column {
        if (!isLoading) {
            val rawMinFreq = normalizedStateFrequency!!.min()
            val rawMaxFreq = normalizedStateFrequency!!.max()

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
                        StateFrequencyFilter(threshold),
                    )
                },
                valueRange = 0f..1f,
            )
            Tooltip({
                Text("This shows only what would happen if the filter is applied to the current epa. If filters are applied before this the effects of this filter change.")
            }) {
                LazyColumn {
                    val states = epa.states.toList().sortedByDescending {
                        normalizedStateFrequency!!.frequencyByState(it)
                    }
                    items(states) { state ->
                        val frequency = normalizedStateFrequency!!.frequencyByState(state)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (frequency < threshold) {
                                Icon(
                                    key = AllIconsKeys.General.Note,
                                    contentDescription = "Below threshold",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text("${state.name.take(15)}:")
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
