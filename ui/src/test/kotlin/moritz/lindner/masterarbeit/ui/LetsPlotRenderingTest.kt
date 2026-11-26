package moritz.lindner.masterarbeit.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.use
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.letsPlot
import org.junit.jupiter.api.Test

/**
 * Guards against binary incompatibilities between lets-plot-compose and the Compose version
 * (e.g. after Dependabot bumps). Such mismatches compile fine but throw
 * NoSuchMethodError / AbstractMethodError / NoClassDefFoundError at render time.
 */
class LetsPlotRenderingTest {
    @Test
    fun `lets-plot PlotPanel renders headless without throwing`() {
        val plot =
            letsPlot(mapOf("x" to listOf(1, 2, 2, 3, 3, 3, 4, 5))) +
                geomHistogram(bins = 5) { x = "x" }

        val errors = mutableListOf<String>()

        ImageComposeScene(width = 400, height = 300) {
            PlotPanel(
                figure = plot,
                modifier = Modifier.fillMaxSize(),
                computationMessagesHandler = { messages -> errors += messages },
            )
        }.use { scene ->
            // Render several frames: layout, plot computation and drawing may span frames.
            repeat(3) { frame -> scene.render(frame * 16_000_000L) }
        }

        assertThat(errors).isEmpty()
    }
}
