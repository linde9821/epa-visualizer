package moritz.lindner.masterarbeit.ui.components

import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
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
) {
    val visitor = TreeBuildingVisitor<Long>()

    scope.launch(backgroundDispatcher) {
        val epa = builder.build()

        epa.acceptDepthFirst(AutomataVisitorProgressBar(visitor, "tree"))

        onEPAConstructed(epa, visitor.root)
    }

    LinearProgressIndicator()
}
