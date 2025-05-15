package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.RadioButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode

data class LayoutSelection(
    val name: String,
    val selected: Boolean,
)

@Composable
fun SingleSelectionList(onLayoutChange: (LayoutSelection) -> Unit) {
    var layouts by remember {
        mutableStateOf(
            listOf(
                LayoutSelection(
                    "Walker",
                    false,
                ),
                LayoutSelection(
                    "Walker Radial Tree",
                    false,
                ),
                LayoutSelection(
                    "Direct Angular Placement",
                    true,
                ),
            ),
        )
    }

    LazyRow {
        itemsIndexed(layouts) { i, item ->
            Text(item.name)
            RadioButton(
                selected = item.selected,
                onClick = {
                    layouts =
                        layouts.map {
                            if (it == item) {
                                it.copy(selected = true)
                            } else {
                                it.copy(selected = false)
                            }
                        }
                    onLayoutChange(item)
                },
            )
        }
    }
}

@Composable
fun EpaTreeViewUi(
    epa: ExtendedPrefixAutomata<Long>,
    tree: EPATreeNode<Long>,
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

            SingleSelectionList {
                layout = it
            }
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
                TidyTreeUi(epa, tree, backgroundDispatcher, radius, margin, layout)
            }
            // TODO: why is this not rendered
            Row(modifier = Modifier.background(Color.Yellow).fillMaxWidth()) {
                Text("UI Component Timeline")
            }
        }
    }
}
