package moritz.lindner.masterarbeit.ui.components.treeview.components.animation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.ui.components.treeview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.treeview.state.EpaViewModel

@Composable
fun WholeCaseAnimationUi(
    filteredEpa: ExtendedPrefixAutomata<Long>,
    viewModel: EpaViewModel,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    Column(modifier = Modifier.Companion.padding(16.dp)) {
        Row {
            Button(onClick = {
                viewModel.updateAnimation(
                    AnimationState.Companion.Empty,
                )
                onClose()
            }) {
                Text("Close")
            }
        }

        Spacer(Modifier.Companion.height(8.dp))

        TimelineSliderWholeLogUi(filteredEpa, backgroundDispatcher, viewModel)
    }
}
