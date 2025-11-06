package moritz.lindner.masterarbeit.ui.components.epaview.state.manager

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowState
import java.util.UUID

data class ManagedWindow(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val windowState: WindowState = WindowState(),
    val content: @Composable (ManagedWindow) -> Unit
)