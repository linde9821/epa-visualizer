package moritz.lindner.masterarbeit.ui.components.epaview.components.filter

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.SimpleTabContent
import org.jetbrains.jewel.ui.component.TabData
import org.jetbrains.jewel.ui.component.TabStrip
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.defaultTabStyle
import org.jetbrains.jewel.ui.typography
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun FilterUi(
    epa: ExtendedPrefixAutomaton<Long>,
    backgroundDispatcher: CoroutineDispatcher,
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    onSetIndex: (Int) -> Unit,
    onApply: (EpaFilter<Long>) -> Unit,
) {
    val tabNames = listOf("Activity", "State Frequency", "Partition Frequency", "Chain Pruning")

    val filterByIndex =
        remember {
            mutableMapOf<Int, EpaFilter<Long>>()
        }

    Column(
        modifier =
            modifier
                .padding(1.dp)
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(4.dp),
                ).padding(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Filters", style = JewelTheme.typography.h1TextStyle)

            DefaultButton(
                onClick = {
                    val combinedFilters =
                        EpaFilter.combine(filterByIndex.values.toList())
                    onApply(combinedFilters)
                },
            ) {
                Row {
                    Icon(Icons.Default.FilterList, contentDescription = "Abort")
                    Text("Apply", color = Color.White, style = JewelTheme.typography.regular,)
                }
            }
        }

        val tabData = tabNames.mapIndexed { index, name ->
            TabData.Default(
                closable = false,
                selected = index == selectedTabIndex,
                content = { tabState ->
                    SimpleTabContent(label = name, state = tabState)
                },
                onClick = { onSetIndex(index) },
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TabStrip(tabs = tabData, style = JewelTheme.defaultTabStyle, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.Companion.height(8.dp))

        Column(modifier = Modifier.padding(8.dp)) {
            when (selectedTabIndex) {
                0 ->
                    ActivityFilterTabUi(epa) {
                        filterByIndex[selectedTabIndex] = it
                    }

                1 ->
                    StateFrequencyFilterUi(epa, backgroundDispatcher) {
                        filterByIndex[selectedTabIndex] = it
                    }

                2 -> {
                    PartitionFrequencyFilterUi(epa, backgroundDispatcher) {
                        filterByIndex[selectedTabIndex] = it
                    }
                }

                else -> {
                    Text("${tabNames[selectedTabIndex]} not implemented", style = JewelTheme.typography.regular,)
                }
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
