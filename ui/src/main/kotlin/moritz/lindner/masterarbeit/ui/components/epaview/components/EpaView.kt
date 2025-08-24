package moritz.lindner.masterarbeit.ui.components.epaview.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.ui.components.epaview.components.animation.AnimationUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.filter.FilterUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.layout.LayoutUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.statistics.StatisticsComparisonUi
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.TreeCanvas
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewStateLower
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaViewStateUpper
import moritz.lindner.masterarbeit.ui.components.epaview.viewmodel.EpaViewModel
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider

@Composable
fun EpaTreeViewUi(
    epa: ExtendedPrefixAutomaton<Long>,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    val epaViewModel by remember {
        mutableStateOf(
            EpaViewModel(
                completeEpa = epa,
                backgroundDispatcher = backgroundDispatcher,
            ),
        )
    }

    val epaUiState by epaViewModel.epaUiState.collectAsState()
    val statisticsState by epaViewModel.statistics.collectAsState()
    val animationState by epaViewModel.animationState.collectAsState()

    var upperState: EpaViewStateUpper by remember { mutableStateOf(EpaViewStateUpper.None) }
    var lowerState: EpaViewStateLower by remember { mutableStateOf(EpaViewStateLower.None) }

    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        TabsUi(
            upperState = upperState,
            onUpperStateChange = { upperState = it },
            lowerState = lowerState,
            onLowerStateChange = { lowerState = it },
            onClose = onClose,
        )

        // OTHER
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(if (lowerState != EpaViewStateUpper.None) 3f else 1f),
            ) {
                // UPPER
                when (upperState) {
                    EpaViewStateUpper.Filter -> {
                        FilterUi(
                            epa = epa,
                            epaUiState = epaUiState,
                            epaViewModel = epaViewModel,
                            backgroundDispatcher = backgroundDispatcher,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                        )
                        Divider(
                            orientation = Orientation.Vertical,
                            modifier = Modifier.fillMaxHeight(),
                            thickness = 1.dp,
                            color = JewelTheme.contentColor.copy(alpha = 0.2f)
                        )
                    }

                    EpaViewStateUpper.Layout -> {
                        LayoutUi(
                            epaViewModel = epaViewModel,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                        ) {
                            epaViewModel.updateLayout(it)
                        }
                        Divider(
                            orientation = Orientation.Vertical,
                            modifier = Modifier.fillMaxHeight(),
                            thickness = 1.dp,
                            color = JewelTheme.contentColor.copy(alpha = 0.2f)
                        )
                    }

                    EpaViewStateUpper.None -> null
                }

                TreeCanvas(
                    epaUiState,
                    animationState,
                    backgroundDispatcher,
                    modifier =
                        Modifier
                            .weight(if (upperState != EpaViewStateUpper.None || lowerState != EpaViewStateLower.None) 2f else 1f)
                            .fillMaxHeight(),
                )
            }

            // LOWER
            if (lowerState != EpaViewStateLower.None) {
                Divider(
                    orientation = Orientation.Horizontal,
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = JewelTheme.contentColor.copy(alpha = 0.2f)
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                ) {
                    when (lowerState) {
                        EpaViewStateLower.Animation -> {
                            AnimationUi(epaUiState.filteredEpa, epaViewModel, backgroundDispatcher)
                        }

                        EpaViewStateLower.Statistics -> {
                            StatisticsComparisonUi(statisticsState)
                        }

                        EpaViewStateLower.None -> null
                    }
                }
            }
        }
    }
}
