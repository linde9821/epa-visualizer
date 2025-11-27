package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

@Stable
class CanvasState {
    var offset by mutableStateOf(Offset.Zero)
    var scale by mutableFloatStateOf(1f)

    var isInitialized by mutableStateOf(false)
        private set

    fun initializeCenter(size: Size) {
        if (!isInitialized) {
            offset = Offset(size.width / 2f, size.height / 2f)
            isInitialized = true
        }
    }
}

@Composable
fun rememberCanvasState() = remember { CanvasState() }
