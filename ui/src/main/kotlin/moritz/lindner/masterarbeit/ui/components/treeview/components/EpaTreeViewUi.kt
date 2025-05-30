package moritz.lindner.masterarbeit.ui.components.treeview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.ui.components.TidyTreeUi
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

    val uiState by viewModel.uiState.collectAsState()
    val statisticsState by viewModel.statistics.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(
                shape = RoundedCornerShape(24.dp),
                onClick = { onClose() },
                modifier = Modifier.Companion.height(48.dp),
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                LayoutOptionUi {
                    viewModel.updateLayout(it)
                }
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(350.dp)
                        .padding(2.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
            ) {
                FilterUi(epa = epa, backgroundDispatcher, onApply = {
                    viewModel.updateFilter(it)
                })
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.8f)
                        .padding(8.dp),
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    elevation = 4.dp,
                ) {
                    TidyTreeUi(uiState)
                }

                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("UI Component Timeline")
                    }
                }
            }

            StatisticsComparisonUi(statisticsState)
        }
    }
}
