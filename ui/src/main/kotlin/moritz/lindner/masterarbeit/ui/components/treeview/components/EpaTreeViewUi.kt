package moritz.lindner.masterarbeit.ui.components.treeview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    val viewModel by remember {
        mutableStateOf(
            EpaViewModel(
                completeEpa = epa,
                backgroundDispatcher = backgroundDispatcher,
            ),
        )
    }

    val uiState by viewModel.epaUiState.collectAsState()
    val statisticsState by viewModel.statistics.collectAsState()
    val animationState by viewModel.animationState.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(
                shape = RoundedCornerShape(24.dp),
                onClick = { onClose() },
                modifier = Modifier.height(48.dp),
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }

            Spacer(Modifier.width(12.dp))

            Surface(
                elevation = 4.dp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                LayoutOptionUi {
                    viewModel.updateLayout(it)
                }
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(350.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
            ) {
                FilterUi(
                    epa = epa,
                    backgroundDispatcher,
                    onApply = {
                        viewModel.updateFilter(it)
                    },
                )
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    elevation = 4.dp,
                ) {
                    TidyTreeUi(uiState, animationState, backgroundDispatcher)
                }

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    AnimationUi(uiState.filteredEpa, viewModel, backgroundDispatcher)
                }
            }

            Surface(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(300.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
            ) {
                StatisticsComparisonUi(statisticsState)
            }
        }
    }
}

sealed class EpaViewStateUpper {
    data object Filter : EpaViewStateUpper()

    data object Layout : EpaViewStateUpper()

    data object None : EpaViewStateUpper()
}

sealed class EpaViewStateLower {
    data object Animation : EpaViewStateLower()

    data object Statistics : EpaViewStateLower()

    data object None : EpaViewStateLower()
}

@Composable
fun Foo(
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
                    EpaViewStateUpper.None -> {}
                }

                // MAP
                TidyTreeUi(
                    epaUiState,
                    animationState,
                    backgroundDispatcher,
                    modifier =
                        Modifier
                            .weight(if (upperState != EpaViewStateUpper.None) 2f else 1f)
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
                        EpaViewStateLower.None -> {
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabsColumnUi(
    upperState: EpaViewStateUpper,
    onUpperStateChange: (EpaViewStateUpper) -> Unit,
    lowerState: EpaViewStateLower,
    onLowerStateChange: (EpaViewStateLower) -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .padding(1.dp)
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(4.dp),
                ).padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            // Close
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }

            // Filter
            IconButton(
                onClick = {
                    onUpperStateChange(
                        if (upperState != EpaViewStateUpper.Filter) EpaViewStateUpper.Filter else EpaViewStateUpper.None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (upperState == EpaViewStateUpper.Filter) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint =
                        if (upperState == EpaViewStateUpper.Filter) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.Unspecified
                        },
                )
            }

            // Layout
            IconButton(
                onClick = {
                    onUpperStateChange(
                        if (upperState != EpaViewStateUpper.Layout) EpaViewStateUpper.Layout else EpaViewStateUpper.None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (upperState == EpaViewStateUpper.Layout) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = "Map",
                    tint =
                        if (upperState == EpaViewStateUpper.Layout) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.Unspecified
                        },
                )
            }
        }

        Column {
            // Animation
            IconButton(
                onClick = {
                    onLowerStateChange(
                        if (lowerState != EpaViewStateLower.Animation) EpaViewStateLower.Animation else EpaViewStateLower.None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (lowerState == EpaViewStateLower.Animation) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    Icons.Default.Animation,
                    contentDescription = "Animation",
                    tint =
                        if (lowerState == EpaViewStateLower.Animation) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.Unspecified
                        },
                )
            }

            // Statistics
            IconButton(
                onClick = {
                    onLowerStateChange(
                        if (lowerState != EpaViewStateLower.Statistics) EpaViewStateLower.Statistics else EpaViewStateLower.None,
                    )
                },
                modifier =
                    Modifier.background(
                        if (lowerState == EpaViewStateLower.Statistics) Color.LightGray else Color.Transparent,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    Icons.Default.Numbers,
                    contentDescription = "Statistics",
                    tint =
                        if (lowerState == EpaViewStateLower.Statistics) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.Unspecified
                        },
                )
            }
        }
    }
}
