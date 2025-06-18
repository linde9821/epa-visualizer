package moritz.lindner.masterarbeit.ui.components.treeview.components.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
    statistics: Statistics<Long>,
) {
    // Apply scroll modifier directly to Row, so it scrolls horizontally
    Row(
        modifier =
            Modifier
                .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                StatisticItem("Partitions", statistics.partitionsCount)
                StatisticItem("States", statistics.stateCount)
                StatisticItem("Events", statistics.eventCount)
                StatisticItem("Cases", statistics.caseCount)
                StatisticItem("Activities", statistics.activityCount)
            }
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
