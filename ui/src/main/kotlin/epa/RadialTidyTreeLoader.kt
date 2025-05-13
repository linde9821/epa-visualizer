package epa

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import moritz.lindner.masterarbeit.drawing.layout.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.drawing.layout.WalkerTreeLayout
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata

@Composable
fun RadialTidyTreeLoader(
    epa: ExtendedPrefixAutomata<Long>,
    tree: EPATreeNode<Long>,
    dispatcher: CoroutineDispatcher,
) {
    var treeLayout: WalkerTreeLayout<Long>? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(epa) {
        withContext(dispatcher) {
            treeLayout = null

            treeLayout = RadialWalkerTreeLayout(100.0f, 80.0f)
            treeLayout!!.build(tree)
            isLoading = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        RadialTidyTree(epa, treeLayout!!)
    }
}
