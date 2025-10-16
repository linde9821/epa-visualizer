package moritz.lindner.masterarbeit.ui.components.epaview.components.toolbar.animation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.EpaStateManager
import moritz.lindner.masterarbeit.ui.components.epaview.state.manager.TabStateManager
import org.jetbrains.jewel.ui.component.CircularProgressIndicatorBig
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text


@Composable
fun AnimationUi(
    epaStateManager: EpaStateManager,
    tabStateManager: TabStateManager,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
) {
    val activeTabId by tabStateManager.activeTabId.collectAsState()
    var state: AnimationSelectionState by remember(activeTabId) { mutableStateOf(AnimationSelectionState.NothingSelected) }

    val epaByTabId by epaStateManager.epaByTabId.collectAsState()
    val epa = activeTabId?.let { epaByTabId[it] }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        if (epa != null && activeTabId != null) {
            when (state) {
                AnimationSelectionState.NothingSelected -> {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DefaultButton(
                            onClick = {
                                state = AnimationSelectionState.SingleCase
                            },
                        ) {
                            Text("Select Case")
                        }

                        DefaultButton(
                            onClick = {
                                state = AnimationSelectionState.WholeLog
                            },
                        ) {
                            Text("Animate whole event log (${epa.eventLogName})")
                        }
                    }
                }

                is AnimationSelectionState.SingleCase -> {
                    SingleCaseAnimationUI(epa, backgroundDispatcher, epaStateManager) {
                        state = AnimationSelectionState.NothingSelected
                    }
                }

                AnimationSelectionState.WholeLog ->
                    TimelineSliderWholeLogUi(epa, epaStateManager, backgroundDispatcher) {
                        state = AnimationSelectionState.NothingSelected
                    }
            }
        } else {
            CircularProgressIndicatorBig()
        }
    }
}

fun findStepSize(
    start: Long,
    end: Long,
    maxSteps: Int = 10_000,
): Long {
    val totalRange = end - start
    if (totalRange <= 0) return 1L

    // Ideal raw step
    val rawStep = totalRange / maxSteps

    // Define preferred "round" step sizes
    val preferredSteps =
        listOf(
            // Millisecond-level
            1L,
            2L,
            5L,
            10L,
            20L,
            50L,
            100L,
            200L,
            250L,
            500L,
            // Second-level
            1_000L, // 1 sec
            2_000L,
            5_000L,
            10_000L,
            15_000L,
            30_000L, // up to 30 sec
            // Minute-level
            60_000L, // 1 min
            120_000L, // 2 min
            300_000L, // 5 min
            600_000L, // 10 min
            900_000L, // 15 min
            1_800_000L, // 30 min
            // Hour-level
            3_600_000L, // 1 hour
            7_200_000L, // 2 hours
            10_800_000L, // 3 hours
            21_600_000L, // 6 hours
            43_200_000L, // 12 hours
            // Day-level
            86_400_000L, // 1 day
            172_800_000L, // 2 days
            604_800_000L, // 7 days (1 week)
        )

    // Pick the smallest preferred step >= rawStep
    return preferredSteps.firstOrNull { it >= rawStep } ?: rawStep
}

