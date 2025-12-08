package moritz.lindner.masterarbeit.ui.components.epaview.components.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.toIntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import moritz.lindner.masterarbeit.ui.logger

@Composable
fun CapturableCanvas(
    modifier: Modifier = Modifier.Companion,
    onCapture: (ImageBitmap) -> Unit,
    shouldCapture: Boolean,
    scope: CoroutineScope,
    content: @Composable () -> Unit
) {
    var hasCaptured by remember(shouldCapture) { mutableStateOf(false) }
    val graphicsLayer = rememberGraphicsLayer()

    Box(
        modifier = modifier
            .drawWithContent {
                graphicsLayer.record(size = size.toIntSize()) {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)

                if (shouldCapture && !hasCaptured) {
                    val width = size.width.toInt()
                    val height = size.height.toInt()

                    if (width > 0 && height > 0) {
                        try {
                            scope.launch {
                                val bitmap = graphicsLayer.toImageBitmap()
                                hasCaptured = true
                                onCapture(bitmap)
                            }
                        } catch (e: Exception) {
                            logger.error(e) { "Error while creating bitmap from canvas" }
                        }
                    }
                }
            }
    ) {
        content()
    }
}