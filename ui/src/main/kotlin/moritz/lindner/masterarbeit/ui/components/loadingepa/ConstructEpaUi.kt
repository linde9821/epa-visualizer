package moritz.lindner.masterarbeit.ui.components.loadingepa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.construction.builder.EpaFromXesBuilder
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.HorizontalProgressBar
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.typography
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun ConstructEpaUi(
    scope: CoroutineScope,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    builder: EpaFromXesBuilder<Long>,
    onEPAConstructed: (ExtendedPrefixAutomaton<Long>) -> Unit,
    onAbort: () -> Unit,
    onError: (String, Throwable) -> Unit,
) {
    var epaConstructionJob by remember { mutableStateOf<Job?>(null) }

    var currentTask by remember { mutableStateOf("") }
    var currentProgress by remember { mutableStateOf(0L) }
    var totalProgress by remember { mutableStateOf(0L) }

    val progressCallback = EpaProgressCallback { current, total, task ->
        currentTask = task
        currentProgress = current
        totalProgress = total
    }

    LaunchedEffect(Unit) {
        epaConstructionJob = scope.launch(backgroundDispatcher) {
            try {
                logger.info { "Start construction" }
                val epa = builder
                    .setProgressCallback(progressCallback)
                    .build()
                logger.info { "construction finished" }
                yield()
                onEPAConstructed(epa)
            } catch (e: NullPointerException) {
                onError("Couldn't parse event log. Check mapper.", e)
            } catch (_: CancellationException) {
                // Job was cancelled, no need to handle
            } catch (e: Exception) {
                onError(e.toString(), e)
            }
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
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Building EPA",
                style = JewelTheme.typography.h2TextStyle
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "This may take a few moments depending on the size of your event log",
                style = JewelTheme.typography.regular
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentTask.ifEmpty { "Initializing..." },
                    style = JewelTheme.typography.h3TextStyle
                )

                val progressPercent = if (totalProgress > 0) {
                    (currentProgress.toFloat() / totalProgress * 100).toInt()
                } else 0

                Text(
                    text = "$progressPercent% • $currentProgress / $totalProgress",
                    style = JewelTheme.typography.small,
                    modifier = Modifier.padding(top = 4.dp)
                )

                HorizontalProgressBar(
                    progress = if (totalProgress > 0) currentProgress.toFloat() / totalProgress else 0f,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
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
                        key = AllIconsKeys.Actions.Close,
                        contentDescription = "Cancel"
                    )
                    Text("Cancel", style = JewelTheme.typography.regular)
                }
            }
        }
    }
}
