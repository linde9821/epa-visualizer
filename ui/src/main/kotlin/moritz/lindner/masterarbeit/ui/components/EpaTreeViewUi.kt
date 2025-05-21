package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode

data class LayoutSelection(
    val name: String,
    val selected: Boolean,
)

@Composable
fun EpaTreeViewUi(
    epa: ExtendedPrefixAutomata<Long>,
    tree: EPATreeNode,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    var radius by remember { mutableStateOf(30.0f) }
    var margin by remember { mutableStateOf(0.0f) }
    var layout by remember {
        mutableStateOf(
            LayoutSelection(
                "Direct Angular Placement",
                true,
            ),
        )
    }
    var showOptions by remember { mutableStateOf(true) }

    Row {
        Button(
            onClick = { onClose() },
        ) {
            Text("Close")
        }

        Column(
            Modifier.padding(20.dp),
        ) {
            if (showOptions) {
                Row {
                    Text("radius (width): ${"%.1f".format(radius)}")
                }
                Row {
                    Slider(
                        value = radius,
                        onValueChange = { radius = it },
                        valueRange = 10.0f..1000.0f,
                    )
                }

                Row {
                    Text("margin (width): ${"%.1f".format(margin)}")
                }
                Row {
                    Slider(
                        value = margin,
                        onValueChange = { margin = it },
                        valueRange = 0.0f..360.0f,
                        modifier = Modifier.weight(1f),
                    )
                }

                SingleSelectionList {
                    layout = it
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier =
                    Modifier.clickable(true) {
                        showOptions = !showOptions
                    },
            )
        }
    }

    Row(modifier = Modifier.background(Color.White).fillMaxWidth()) {
        Column(
            modifier = Modifier.background(Color.Red).fillMaxWidth(0.1f).fillMaxHeight(),
        ) {
            Text("UI Component Filter")
        }
        Column(
            modifier = Modifier.background(Color.Blue).fillMaxSize(),
        ) {
            Row(modifier = Modifier.background(Color.White).fillMaxWidth().fillMaxHeight(0.9f)) {
                TidyTreeUi(epa, tree, backgroundDispatcher, radius, margin, layout)
            }
            Row(modifier = Modifier.background(Color.Yellow).fillMaxWidth()) {
                Text("UI Component Timeline")
            }
        }
    }
}
