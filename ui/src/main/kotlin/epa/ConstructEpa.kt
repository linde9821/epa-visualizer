package epa

import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.launch
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder

@Composable
fun ConstructEpa(
    scope: CoroutineScope,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    builder: ExtendedPrefixAutomataBuilder<Long>,
    onEPAConstructed: (ExtendedPrefixAutomata<Long>) -> Unit,
) {
    scope.launch(backgroundDispatcher) {
        val epa = builder.build()
        onEPAConstructed(epa)
    }

    LinearProgressIndicator()
}
