package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moritz.lindner.masterarbeit.ui.components.epaview.state.StatisticsState
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager
import org.jetbrains.jewel.ui.component.CircularProgressIndicatorBig

@Composable
fun StatisticsComparisonUi(tabStateManager: TabStateManager, epaStateManager: EpaStateManager) {

    val tabsState by tabStateManager.tabs.collectAsState()
    val activeTabId by tabStateManager.activeTabId.collectAsState()
    val statisticsByTabId by epaStateManager.statisticsByTabId.collectAsState()

    val currentTab = remember(tabsState, activeTabId) {
        tabsState.find { it.id == activeTabId }
    }

    val currentStatistics = remember(statisticsByTabId, activeTabId) {
        statisticsByTabId[activeTabId]
    }

    val rootStatistics = remember(statisticsByTabId) {
        statisticsByTabId.toList().firstOrNull()?.second
    }

    val statisticsState = rootStatistics?.let {
        StatisticsState(
            fullEpa = rootStatistics,
            filteredEpa = currentStatistics
        )
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (statisticsState != null) {
                StatisticsElement(
                    title = "Root EPA",
                    statistics = statisticsState.fullEpa,
                    modifier = Modifier.weight(1f),
                )
                if (statisticsState.filteredEpa != null) {
                    StatisticsElement(
                        title = "${currentTab?.title} EPA",
                        statistics = statisticsState.filteredEpa,
                        modifier = Modifier.weight(1f),
                    )
                } else CircularProgressIndicatorBig()
            } else {
                CircularProgressIndicatorBig()
            }
        }
    }
}
