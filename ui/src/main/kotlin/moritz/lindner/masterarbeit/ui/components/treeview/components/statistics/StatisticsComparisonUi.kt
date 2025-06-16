package moritz.lindner.masterarbeit.ui.components.treeview.components.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.ui.components.treeview.state.StatisticsState

@Composable
fun StatisticsComparisonUi(statisticsState: StatisticsState<Long>?) {
    if (statisticsState == null) {
        CircularProgressIndicator(
            modifier = Modifier.Companion.size(50.dp),
            strokeWidth = 6.dp,
            color = MaterialTheme.colors.primary,
        )
    } else {
        Column(
            modifier =
                Modifier.Companion
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatisticsCard("Complete EPA", statisticsState.fullEpa)

            if (statisticsState.filteredEpa != null) {
                StatisticsCard("Filtered EPA", statisticsState.filteredEpa)
            }
        }
    }
}
