package moritz.lindner.masterarbeit.ui.components.loadingepa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomatonBuilder
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun ConstructEpaUi(
    scope: CoroutineScope,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    builder: ExtendedPrefixAutomatonBuilder<Long>,
    onEPAConstructed: (ExtendedPrefixAutomaton<Long>) -> Unit,
    onAbort: () -> Unit,
    onError: (String, Throwable) -> Unit,
) {
    var epaConstructionJob by remember { mutableStateOf<Job?>(null) }

    epaConstructionJob =
        scope.launch(backgroundDispatcher) {
            try {
                val epa = builder.build()
                yield()
                onEPAConstructed(epa)
            } catch (e: NullPointerException) {
                onError("Check Mapper", e)
            } catch (_: CancellationException) {
                // Job was cancelled, no need to handle
            } catch (e: Exception) {
                onError(e.toString(), e)
            }
        }

    // Ensure job is cancelled when the composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            epaConstructionJob?.cancel()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(45.dp),
            )

            AnimatedLoadingText(
                baseText = "Constructing EPA"
            )

            DefaultButton(
                onClick = {
                    epaConstructionJob?.cancel()
                    epaConstructionJob = null
                    onAbort()
                },
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        tint = JewelTheme.contentColor,
                        contentDescription = "Abort"
                    )
                    Text("Abort")
                }
            }
        }
    }
}
