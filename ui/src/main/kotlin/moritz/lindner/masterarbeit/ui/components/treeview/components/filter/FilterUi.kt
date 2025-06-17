package moritz.lindner.masterarbeit.ui.components.treeview.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.filter.EpaFilter
import moritz.lindner.masterarbeit.ui.logger
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun FilterUi(
    epa: ExtendedPrefixAutomata<Long>,
    backgroundDispatcher: CoroutineDispatcher,
    modifier: Modifier = Modifier,
    onApply: (EpaFilter<Long>) -> Unit,
) {
    val tabs = listOf("Activity", "State Frequency", "Partition Frequency", "Chain Pruning")
    var selectedIndex by remember { mutableStateOf(0) }

    val filterByIndex =
        remember {
            mutableMapOf<Int, EpaFilter<Long>>()
        }

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = {
                    val combinedFilters =
                        EpaFilter.combine(
                            filterByIndex.values.toList().onEach { filter ->
                                logger.info { "Filter $filter is combined" }
                            },
                        )
                    onApply(combinedFilters)
                },
                shape = RoundedCornerShape(24.dp),
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

        // TODO: fix reset by using state hoisting
        when (selectedIndex) {
            0 ->
                ActivityFilterTabUi(epa) {
                    filterByIndex[selectedIndex] = it
                }

            1 ->
                StateFrequencyFilterUi(epa, backgroundDispatcher) {
                    filterByIndex[selectedIndex] = it
                }

            2 -> {
                PartitionFrequencyFilterUi(epa, backgroundDispatcher) {
                    filterByIndex[selectedIndex] = it
                }
            }

            else -> {
                Text("TODO: implement ${tabs[selectedIndex]}")
            }
        }
    }
}

fun sliderToThreshold(
    sliderValue: Float,
    minThreshold: Float,
    maxThreshold: Float,
): Float {
    if (minThreshold <= 0f || maxThreshold <= 0f || minThreshold >= maxThreshold) {
        return minThreshold
    }

    val rangeLog = log10(maxThreshold / minThreshold)
    return 10f.pow(sliderValue * rangeLog) * minThreshold
}
