package epa

import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.launch
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.visitor.AutomataVisitorProgressBar
import moritz.lindner.masterarbeit.treelayout.tree.EPATreeNode
import moritz.lindner.masterarbeit.treelayout.tree.TreeBuildingVisitor

@Composable
fun ConstructEpa(
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
