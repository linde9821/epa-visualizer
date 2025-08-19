package moritz.lindner.masterarbeit.ui.components.epaview.components.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.features.statistics.Statistics
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun StatisticsElement(
    title: String,
    statistics: Statistics<Long>?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
                .fillMaxWidth()
                .padding(4.dp),
    ) {
        Text(
            text = title,
            style = JewelTheme.typography.h4TextStyle,
            modifier = Modifier.padding(bottom = 3.dp),
        )

        if (statistics != null) {
            Column (
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        StatisticItem("Partitions", statistics.partitionsCount)
                        StatisticItem("States", statistics.stateCount)
                        StatisticItem("Events", statistics.eventCount)
                        StatisticItem("Cases", statistics.caseCount)
                        StatisticItem("Activities", statistics.activityCount)
                    }

                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            "Top 4 Activities:",
                            style = JewelTheme.typography.regular,
                        )

                        statistics.activityFrequency
                            .toList()
                            .sortedByDescending { it.second }
                            .take(4)
                            .forEach { (activity, frequency) ->
                                StatisticItem(activity.toString(), frequency)
                            }
                    }
                }

                Row {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            "Time",
                            style = JewelTheme.typography.regular,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        StatisticItem("First Event", statistics.interval.first)
                        StatisticItem("Last Event", statistics.interval.second)
                    }

                }
            }
        }
    }
}
