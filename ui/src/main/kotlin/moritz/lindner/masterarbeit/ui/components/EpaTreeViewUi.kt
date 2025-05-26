package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.drawing.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode
import moritz.lindner.masterarbeit.epa.drawing.tree.TreeBuildingVisitor
import kotlin.math.PI

data class LayoutSelection<T : TreeLayout>(
    val name: String,
    val construct: () -> T,
)

private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

@Composable
fun EpaTreeViewUi(
    epa: ExtendedPrefixAutomata<Long>,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    val logger = remember { KotlinLogging.logger { } }

    val mutex = remember { Mutex() }

    var radius by remember { mutableStateOf(120.0f) }
    var margin by remember { mutableStateOf(3.0f) }
    val layouts =
        listOf(
            LayoutSelection(
                "Direct Angular Placement",
            ) {
                DirectAngularPlacementTreeLayout(radius, epa.states.size)
            },
            LayoutSelection("Walker") {
                WalkerTreeLayout(
                    distance = margin,
                    yDistance = radius,
                    expectedCapacity = epa.states.size,
                )
            },
            LayoutSelection("Walker Radial Tree") {
                RadialWalkerTreeLayout(
                    radius,
                    epa.states.size,
                    margin = margin.degreesToRadians(),
                )
            },
        )

    var layoutSelection by remember { mutableStateOf(layouts.first()) }
    var showLayoutOptions by remember { mutableStateOf(false) }

    var tree by remember { mutableStateOf<EPATreeNode?>(null) }
    var treeLayout by remember { mutableStateOf<TreeLayout?>(null) }

    LaunchedEffect(epa, layoutSelection, radius, margin) {
        logger.info { "building layout" }
        // TODO: filter epa

        // build tree
        val treeVisitor = TreeBuildingVisitor<Long>()
        epa.acceptDepthFirst(treeVisitor)

        tree = treeVisitor.root

        // build layout
        treeLayout = layoutSelection.construct()
        treeLayout!!.build(tree!!)
        logger.info { "layout build" }
    }

    Row {
        Button(
            onClick = { onClose() },
        ) {
            Text("Close")
        }

        Column(
            Modifier.padding(20.dp),
        ) {
            if (showLayoutOptions) {
                LayoutOptions(
                    radius = radius,
                    onRadiusChange = { radius = it },
                    margin = margin,
                    onMarginChange = { margin = it },
                    layouts = layouts,
                    onLayoutSelectionChange = { layoutSelection = it },
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier =
                    Modifier.clickable(true) {
                        showLayoutOptions = !showLayoutOptions
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
                if (treeLayout != null) {
                    TidyTreeUi(
                        layout = treeLayout!!,
                    )
                }
            }
            Row(modifier = Modifier.background(Color.Yellow).fillMaxWidth()) {
                Text("UI Component Timeline")
            }
        }
    }
}

@Composable
fun LayoutOptions(
    radius: Float,
    onRadiusChange: (Float) -> Unit,
    margin: Float,
    onMarginChange: (Float) -> Unit,
    layouts: List<LayoutSelection<*>>,
    onLayoutSelectionChange: (LayoutSelection<*>) -> Unit,
) {
    Row {
        Text("radius (width): ${"%.1f".format(radius)}")
    }
    Row {
        Slider(
            value = radius,
            onValueChange = { onRadiusChange(it) },
            valueRange = 10.0f..1000.0f,
        )
    }

    Row {
        Text("margin (width): ${"%.1f".format(margin)}")
    }
    Row {
        Slider(
            value = margin,
            onValueChange = { onMarginChange(it) },
            valueRange = 0.0f..360.0f,
            modifier = Modifier.weight(1f),
        )
    }

    RadioButtonSingleSelectionColumn(layouts.map { option -> Pair(option, option.name) }) { layout, _ ->
        onLayoutSelectionChange(layout)
    }
}
