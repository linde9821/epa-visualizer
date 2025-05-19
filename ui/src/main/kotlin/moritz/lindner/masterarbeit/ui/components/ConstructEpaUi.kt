package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.launch
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode
import moritz.lindner.masterarbeit.epa.drawing.tree.TreeBuildingVisitor
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitorProgressBar

@Composable
fun ConstructEpaUi(
    scope: CoroutineScope,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    builder: ExtendedPrefixAutomataBuilder<Long>,
    onEPAConstructed: (ExtendedPrefixAutomata<Long>, EPATreeNode<Long>) -> Unit,
    onError: (String, Throwable) -> Unit,
) {
    val visitor = TreeBuildingVisitor<Long>()

    scope.launch(backgroundDispatcher) {
        try {
            val epa = builder.build()
            epa.acceptDepthFirst(AutomataVisitorProgressBar(visitor, "tree"))
            onEPAConstructed(epa, visitor.root)
        } catch (e: NullPointerException) {
            onError("Check Mapper", e)
        } catch (e: Exception) {
            onError(e.toString(), e)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(200.dp),
            strokeWidth = 5.dp,
        )
    }
}
