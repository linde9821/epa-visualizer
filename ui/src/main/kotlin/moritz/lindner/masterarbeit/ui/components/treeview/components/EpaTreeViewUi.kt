package moritz.lindner.masterarbeit.ui.components.treeview.components

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
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.ui.components.treeview.components.animation.AnimationUi
import moritz.lindner.masterarbeit.ui.components.treeview.components.filter.FilterUi
import moritz.lindner.masterarbeit.ui.components.treeview.components.layout.LayoutOptionUi
import moritz.lindner.masterarbeit.ui.components.treeview.components.statistics.StatisticsComparisonUi
import moritz.lindner.masterarbeit.ui.components.treeview.state.EpaViewModel
import kotlin.math.PI

fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

@Composable
fun EpaTreeViewUi(
    epa: ExtendedPrefixAutomata<Long>,
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
        TabsColumnUi(
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
                            backgroundDispatcher,
                            onApply = {
                                epaViewModel.updateFilter(it)
                            },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                        )
                    }

                    EpaViewStateUpper.Layout ->
                        LayoutOptionUi(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                        ) {
                            epaViewModel.updateLayout(it)
                        }

                    EpaViewStateUpper.None -> null
                }

                TidyTreeUi(
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
