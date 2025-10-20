package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

@Stable
class CanvasState {
    var offset by mutableStateOf(Offset.Companion.Zero)
    var scale by mutableFloatStateOf(1f)
}

@Composable
fun rememberCanvasState() = remember { CanvasState() }
