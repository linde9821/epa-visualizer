package moritz.lindner.masterarbeit.ui.components.epaview.components.project

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.animation.AnimationUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.statistics.StatisticsComparisonUi
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewLowerState
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager

@Composable
fun LowerLayout(
    lowerState: EpaViewLowerState,
    tabStateManager: TabStateManager,
    epaStateManager: EpaStateManager,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit
) {
    when (lowerState) {
        EpaViewLowerState.Animation -> PanelMenu(
            title = "Animation",
            down = true,
            onClose = { onClose() },
            modifier = Modifier.padding(8.dp),
        ) {
            AnimationUi(
                epaStateManager = epaStateManager,
                tabStateManager = tabStateManager,
                backgroundDispatcher = backgroundDispatcher
            )
        }

        EpaViewLowerState.Statistics -> PanelMenu(
            title = "Statistics",
            down = true,
            onClose = { onClose() },
            modifier = Modifier.padding(8.dp),
        ) {
            StatisticsComparisonUi(
                tabStateManager,
                epaStateManager,
            )
        }

        EpaViewLowerState.None -> {}
    }
}