package moritz.lindner.masterarbeit.ui.components.epaview.state.manager

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WindowManager {
    private val _windows = MutableStateFlow<List<ManagedWindow>>(emptyList())
    val windows = _windows.asStateFlow()

    fun openWindow(
        title: String,
        windowState: WindowState = WindowState(),
        content: @Composable (ManagedWindow) -> Unit
    ): String {
        val window = ManagedWindow(
            title = title,
            windowState = windowState,
            content = content
        )
        _windows.update { it + window }
        return window.id
    }

    fun closeWindow(windowId: String) {
        _windows.update { it.filterNot { window -> window.id == windowId } }
    }

    fun closeWindow(window: ManagedWindow) {
        closeWindow(window.id)
    }

    fun updateWindowTitle(windowId: String, newTitle: String) {
        _windows.update { windows ->
            windows.map { window ->
                if (window.id == windowId) window.copy(title = newTitle)
                else window
            }
        }
    }
}