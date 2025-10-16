package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.features.statistics.Statistics
import moritz.lindner.masterarbeit.ui.common.Formatting.asFormattedLocalDateTime
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
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        StatisticItem("Events", statistics.eventCount)
                        StatisticItem("States", statistics.stateCount)
                        StatisticItem("Traces", statistics.caseCount)
                        StatisticItem("Partitions", statistics.partitionsCount)
                        StatisticItem("Activities", statistics.activityCount)
                        StatisticItem("Transitions", statistics.transitions)
                    }

                    Column(
                        modifier = Modifier.weight(1f)
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
                            "Eventlog:",
                            style = JewelTheme.typography.regular,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        StatisticItem(
                            "First Event",
                            statistics.interval.first?.asFormattedLocalDateTime() ?: "not present"
                        )
                        StatisticItem(
                            "Last Event",
                            statistics.interval.second?.asFormattedLocalDateTime() ?: "not present"
                        )
                    }

                }
            }
        }
    }
}
