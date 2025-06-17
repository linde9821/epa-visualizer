package moritz.lindner.masterarbeit.ui.components.treeview.components.animation

import androidx.compose.runtime.Composable
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.ui.components.treeview.state.EpaViewModel

@Composable
fun WholeCaseAnimationUi(
    filteredEpa: ExtendedPrefixAutomata<Long>,
    viewModel: EpaViewModel,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    TimelineSliderWholeLogUi(filteredEpa, backgroundDispatcher, viewModel, onClose)
}
