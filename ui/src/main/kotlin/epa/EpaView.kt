package epa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.drawing.tree.EPATreeNode
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata

@Composable
fun EpaView(
    epa: ExtendedPrefixAutomata<Long>,
    tree: EPATreeNode<Long>,
    scope: CoroutineScope,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    var radius by remember { mutableStateOf(30.0f) }
    var margin by remember { mutableStateOf(0.0f) }

    Row {
        Button(
            onClick = { onClose() },
        ) {
            Text("Close")
        }

        Column {
            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 10.0f..1000.0f,
            )

            Slider(
                value = margin,
                onValueChange = { margin = it },
                valueRange = 0.0f..360.0f,
            )
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
                RadialTidyTree(epa, tree, backgroundDispatcher, scope, radius, margin)
            }
            // TODO: why is this not rendered
            Row(modifier = Modifier.background(Color.Yellow).fillMaxWidth()) {
                Text("UI Component Timeline")
            }
        }
    }
}
