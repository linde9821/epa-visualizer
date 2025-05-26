package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.drawing.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.tree.EPATreeNode
import moritz.lindner.masterarbeit.epa.drawing.tree.TreeBuildingVisitor
import moritz.lindner.masterarbeit.epa.filter.ActivityFilter
import moritz.lindner.masterarbeit.epa.filter.EpaFilter
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

    var filter by remember { mutableStateOf<EpaFilter<Long>?>(null) }

    var tree by remember { mutableStateOf<EPATreeNode?>(null) }
    var treeLayout by remember { mutableStateOf<TreeLayout?>(null) }

    LaunchedEffect(epa, layoutSelection, radius, margin, filter) {
        // filter epa
        val filteredEpa =
            if (filter != null) {
                filter!!.apply(epa)
            } else {
                epa
            }

        logger.info { "building layout" }

        // build tree
        val treeVisitor = TreeBuildingVisitor<Long>()
        filteredEpa.acceptDepthFirst(treeVisitor)

        tree = treeVisitor.root

        // build layout
        treeLayout = layoutSelection.construct()
        treeLayout!!.build(tree!!)
        logger.info { "layout build" }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    shape = RoundedCornerShape(24.dp),
                    onClick = onClose,
                    modifier = Modifier.height(48.dp),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }

                Column(horizontalAlignment = Alignment.End) {
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
                        contentDescription = "Toggle layout options",
                        modifier =
                            Modifier
                                .clickable { showLayoutOptions = !showLayoutOptions }
                                .padding(top = 8.dp),
                    )
                }
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(250.dp)
                        .padding(8.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
            ) {
                FilterUi(epa = epa, onApply = { filter = it })
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp),
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    elevation = 4.dp,
                ) {
                    if (treeLayout != null) {
                        TidyTreeUi(layout = treeLayout!!)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("No tree layout available")
                        }
                    }
                }

//                Surface(
//                    modifier =
//                        Modifier
//                            .fillMaxWidth()
//                            .height(80.dp),
//                    elevation = 4.dp,
//                    shape = RoundedCornerShape(12.dp),
//                ) {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center,
//                    ) {
//                        Text("UI Component Timeline")
//                    }
//                }
            }
        }
    }
}

@Composable
fun FilterUi(
    epa: ExtendedPrefixAutomata<Long>,
    onApply: (EpaFilter<Long>) -> Unit,
) {
    val activities =
        remember {
            mutableStateListOf(*epa.activities.map { it to true }.toTypedArray())
        }

    val tabs = listOf("Activity Filter", "Future Filter")
    var selectedIndex by remember { mutableStateOf(0) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                shape = RoundedCornerShape(24.dp),
                onClick = {
                    val selectedActivities = activities.filter { it.second }.map { it.first }
                    val filters = ActivityFilter<Long>(selectedActivities.toHashSet())
                    onApply(filters)
                },
                modifier = Modifier.height(48.dp),
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Abort")
                Spacer(Modifier.width(8.dp))
                Text("Apply", color = Color.White, style = MaterialTheme.typography.button)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(selectedTabIndex = selectedIndex, backgroundColor = Color.White) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    text = { Text(title) },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedIndex) {
            0 -> ActivityFilterTab(activities)
            else -> {}
        }
    }
}

@Composable
fun ActivityFilterTab(activities: SnapshotStateList<Pair<Activity, Boolean>>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(activities) { index, (activity, enabled) ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = activity.name)
                Checkbox(
                    checked = enabled,
                    onCheckedChange = {
                        activities[index] = activity to it
                    },
                )
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
