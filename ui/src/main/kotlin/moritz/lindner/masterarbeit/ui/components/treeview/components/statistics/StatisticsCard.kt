package moritz.lindner.masterarbeit.ui.components.treeview.components.statistics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.epa.visitor.statistics.Statistics

@Composable
fun StatisticsCard(
    title: String,
    statistics: Statistics,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
    ) {
        Box(modifier = Modifier.Companion.horizontalScroll(rememberScrollState())) {
            Column(modifier = Modifier.Companion.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.Companion.padding(bottom = 8.dp),
                )

                StatisticItem("Partitions", statistics.partitionsCount)
                StatisticItem("States", statistics.stateCount)
                StatisticItem("Events", statistics.eventCount)
                StatisticItem("Cases", statistics.caseCount)
                StatisticItem("Activities", statistics.activityCount)

                Spacer(modifier = Modifier.Companion.height(12.dp))

                Text("Top 5 Activities", style = MaterialTheme.typography.subtitle2)
                Spacer(modifier = Modifier.Companion.height(4.dp))

                statistics.activityFrequency
                    .toList()
                    .sortedByDescending { it.second }
                    .take(5)
                    .forEach { (activity, frequency) ->
                        StatisticItem(activity.toString(), frequency)
                    }
            }
        }
    }
}
