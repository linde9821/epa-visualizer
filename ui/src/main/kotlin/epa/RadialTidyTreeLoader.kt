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
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.treelayout.TreeLayout
import moritz.lindner.masterarbeit.treelayout.tree.EPATreeNode

@Composable
fun RadialTidyTreeLoader(
    epa: ExtendedPrefixAutomata<Long>,
    tree: EPATreeNode<Long>,
    dispatcher: CoroutineDispatcher,
) {
    var treeLayout: TreeLayout<Long>? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(epa) {
        withContext(dispatcher) {
            treeLayout = null

            treeLayout = TreeLayout(tree, 10.0f, 600.0f)
            treeLayout!!.build()
            isLoading = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        RadialTidyTree(epa, treeLayout!!)
    }
}
