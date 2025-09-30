package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.ui.components.epaview.state.StatisticsState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.typography

@Composable
fun StatisticsComparisonUi(statisticsState: StatisticsState<Long>?) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Statistics", style = JewelTheme.typography.h1TextStyle)

            if (statisticsState == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        if (statisticsState != null) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
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
