package moritz.lindner.masterarbeit.ui.components.treeview.components.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.visitor.statistics.Statistics

@Composable
fun StatisticsElement(
    title: String,
    statistics: Statistics<Long>?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxHeight() // make sure the Box takes full height for scrolling to work
                .verticalScroll(rememberScrollState())
                .padding(6.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            if (statistics != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        StatisticItem("Partitions", statistics.partitionsCount)
                        StatisticItem("States", statistics.stateCount)
                        StatisticItem("Events", statistics.eventCount)
                        StatisticItem("Cases", statistics.caseCount)
                        StatisticItem("Activities", statistics.activityCount)
                    }

                    VerticalDivider()

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            "Top 4 Activities",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )

                        statistics.activityFrequency
                            .toList()
                            .sortedByDescending { it.second }
                            .take(4)
                            .forEach { (activity, frequency) ->
                                StatisticItem(activity.toString(), frequency)
                            }
                    }

                    VerticalDivider()

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            "Time",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        StatisticItem("First Event", statistics.interval.first)
                        StatisticItem("Last Event", statistics.interval.second)
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalDivider() {
    Divider(
        modifier =
            Modifier
                .fillMaxHeight()
                .width(1.dp),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
    )
}
