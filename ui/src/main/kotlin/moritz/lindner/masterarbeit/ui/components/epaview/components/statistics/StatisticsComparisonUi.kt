package moritz.lindner.masterarbeit.ui.components.epaview.components.statistics

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.ui.components.epaview.state.StatisticsState

@Composable
fun StatisticsComparisonUi(statisticsState: StatisticsState<Long>?) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(1.dp)
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(8.dp),
                ).padding(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Statistics", style = MaterialTheme.typography.h4)

            if (statisticsState == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colors.primary,
                )
            }
        }

        if (statisticsState != null) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize() // take full screen or parent size
                        .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatisticsElement(
                    title = "Complete EPA",
                    statistics = statisticsState.fullEpa,
                    modifier = Modifier.weight(1f),
                )
                StatisticsElement(
                    title = "Filtered EPA",
                    statistics = statisticsState.filteredEpa,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
