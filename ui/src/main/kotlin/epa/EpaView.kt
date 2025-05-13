package epa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.treelayout.tree.EPATreeNode

@Composable
fun EpaView(
    epa: ExtendedPrefixAutomata<Long>,
    tree: EPATreeNode<Long>,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    Row {
        Button(
            onClick = { onClose() },
        ) {
            Text("Close")
        }
    }
    Row(modifier = Modifier.background(Color.White).fillMaxWidth()) {
        Column(
            modifier = Modifier.background(Color.Red).fillMaxWidth(0.2f).fillMaxHeight(),
        ) {
            Text("UI Component Filter")
        }
        Column(
            modifier = Modifier.background(Color.Blue).fillMaxSize(),
        ) {
            Row(modifier = Modifier.background(Color.White).fillMaxWidth()) {
                RadialTidyTreeLoader(epa, tree, backgroundDispatcher)
            }
            // TODO: why is this not rendered
            Row(modifier = Modifier.background(Color.Yellow).fillMaxWidth()) {
                Text("UI Component Timeline")
            }
        }
    }
}
