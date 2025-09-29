package moritz.lindner.masterarbeit.ui.components.epaview.components.animation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.AnimationService
import moritz.lindner.masterarbeit.epa.features.animation.EventLogAnimation
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
//import moritz.lindner.masterarbeit.ui.components.epaview.viewmodel.EpaViewModel
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.Slider
import kotlin.math.roundToInt

//@Composable
//fun TimelineSliderSingleCaseUi(
//    epa: ExtendedPrefixAutomaton<Long>,
//    dispatcher: CoroutineDispatcher,
//    viewModel: EpaViewModel,
//    caseId: String,
//) {
//    val animationService = AnimationService<Long>()
//
//    var isLoading by remember { mutableStateOf(true) }
//    var animation by remember { mutableStateOf<EventLogAnimation<Long>?>(null) }
//    var sliderValue by remember { mutableStateOf(0f) }
//
//    LaunchedEffect(epa) {
//        isLoading = true
//        withContext(dispatcher) {
//            animation = animationService.createCaseAnimation(
//                epa,
//                caseId
//            )
//
//            yield()
//
//            viewModel.updateAnimation(
//                AnimationState.Empty,
//            )
//        }
//        isLoading = false
//    }
//
//    if (isLoading) {
//        CircularProgressIndicator()
//    } else if (animation != null) {
//        Slider(
//            value = sliderValue,
//            onValueChange = { newValue ->
//                sliderValue = newValue
//
//                val index = sliderValue.roundToInt().coerceIn(0, animation!!.totalAmountOfEvents - 1)
//                val state = animation!!.getNthEntry(index)
//
//                logger.info { "Getting state at $index" }
//
//                val animationState =
//                    if (state == null) {
//                        AnimationState.Empty
//                    } else {
//                        AnimationState(
//                            time = state.first,
//                            currentTimeStates = setOf(state.second),
//                        )
//                    }
//                viewModel.updateAnimation(animationState)
//            },
//            modifier = Modifier.fillMaxWidth(),
//            valueRange = 0f..(animation!!.totalAmountOfEvents.toFloat() - 1f),
//            steps = animation!!.totalAmountOfEvents - 1,
//        )
//    }
//}