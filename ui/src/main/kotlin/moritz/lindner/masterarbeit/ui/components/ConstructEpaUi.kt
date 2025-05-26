package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun ConstructEpaUi(
    scope: CoroutineScope,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    builder: ExtendedPrefixAutomataBuilder<Long>,
    onEPAConstructed: (ExtendedPrefixAutomata<Long>) -> Unit,
    onAbort: () -> Unit,
    onError: (String, Throwable) -> Unit,
) {
    var epaConstructionJob by remember { mutableStateOf<Job?>(null) }

    epaConstructionJob =
        scope.launch(backgroundDispatcher) {
            try {
                val epa = builder.build()
                yield()
                onEPAConstructed.invoke(epa)
            } catch (e: NullPointerException) {
                onError("Check Mapper", e)
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                onError(e.toString(), e)
            }
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            color = MaterialTheme.colors.surface,
            modifier =
                Modifier
                    .width(300.dp)
                    .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colors.primary,
                )
                Text(
                    text = "Constructing EPA...",
                    style = MaterialTheme.typography.subtitle1,
                )

                Button(
                    shape = RoundedCornerShape(24.dp),
                    onClick = {
                        epaConstructionJob?.cancel()
                        epaConstructionJob = null
                        onAbort()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F)),
                    modifier = Modifier.height(48.dp),
                ) {
                    Icon(Icons.Default.StopCircle, contentDescription = "Abort")
                    Spacer(Modifier.width(8.dp))
                    Text("Abort", color = Color.White, style = MaterialTheme.typography.button)
                }
            }
        }
    }
}
