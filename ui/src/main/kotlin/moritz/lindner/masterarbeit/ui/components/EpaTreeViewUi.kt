package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.domain.Activity
import moritz.lindner.masterarbeit.epa.drawing.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.DirectAngularPlacementTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.WalkerTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.tree.TreeBuildingVisitor
import moritz.lindner.masterarbeit.epa.filter.ActivityFilter
import moritz.lindner.masterarbeit.epa.filter.DoNothingFilter
import moritz.lindner.masterarbeit.epa.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.filter.PartitionFrequencyFilter
import moritz.lindner.masterarbeit.epa.visitor.statistics.NormalizedPartitionFrequencyVisitor
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.PI

data class LayoutSelection(
    val name: String,
)

private fun Float.degreesToRadians() = this * PI.toFloat() / 180.0f

data class LayoutConfig(
    val radius: Float,
    val margin: Float,
    val layout: LayoutSelection,
)

object TreeLayoutConstructionHelper {
    fun build(
        config: LayoutConfig,
        epa: ExtendedPrefixAutomata<Long>,
    ): TreeLayout =
        when (config.layout.name) {
            "Walker Radial Tree" -> {
                RadialWalkerTreeLayout(
                    layerSpace = config.radius,
                    expectedCapacity = epa.states.size,
                    margin = config.margin,
                )
            }

            "Walker" -> {
                WalkerTreeLayout(
                    distance = config.margin,
                    yDistance = config.radius,
                    expectedCapacity = epa.states.size,
                )
            }

            "Direct Angular Placement" -> {
                DirectAngularPlacementTreeLayout(config.radius, epa.states.size)
            }

            else -> {
                TODO()
            }
        }
}

data class UiState(
    val layout: TreeLayout?,
    val isLoading: Boolean,
    val statistics: Float?,
)

class EpaViewModel(
    val completeEpa: ExtendedPrefixAutomata<Long>,
    val backgroundDispatcher: ExecutorCoroutineDispatcher,
) {
    private val _filter: MutableStateFlow<EpaFilter<Long>> = MutableStateFlow(DoNothingFilter<Long>())
    val filter: StateFlow<EpaFilter<Long>> = _filter

    fun updateFilter(filter: EpaFilter<Long>) {
        _filter.value = filter
    }

    fun updateLayout(layoutConfig: LayoutConfig) {
        _layout.value = layoutConfig
    }

    private val _layout =
        MutableStateFlow(
            LayoutConfig(
                200.0f,
                5f.degreesToRadians(),
                LayoutSelection("Walker Radial Tree"),
            ),
        )
    val layout: StateFlow<LayoutConfig> = _layout

    private val _uiState =
        MutableStateFlow(
            UiState(
                null,
                true,
                null,
            ),
        )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        CoroutineScope(backgroundDispatcher + SupervisorJob()).launch {
            var lastFilter: EpaFilter<Long>? = null
            var lastLayoutConfig: LayoutConfig? = null
            var lastFilterEpa: ExtendedPrefixAutomata<Long>? = null
            var lastLayout: TreeLayout? = null

            combine(
                _filter,
                _layout.distinctUntilChanged { a, b ->
                    a.radius == b.radius && a.margin == b.margin && a.layout == b.layout
                },
            ) { filter, layout ->
                Pair(filter, layout)
            }.collectLatest { (filter, layoutConfig) ->
                logger.info { "running state update" }
                logger.info { "filter: ${filter.name()}" }
                logger.info { "layout: $layoutConfig" }
                _uiState.update { it.copy(isLoading = true) }

                try {
                    val layout =
                        withContext(backgroundDispatcher) {
                            val filteredEpa = filter.apply(completeEpa.copy())
                            logger.info { "Prefilter ${completeEpa.states.size}\nPostfilter ${filteredEpa.states.size}\n" }
                            yield()

                            val layout = TreeLayoutConstructionHelper.build(layoutConfig, filteredEpa)
                            yield()

                            val treeVisitor = TreeBuildingVisitor<Long>()
                            filteredEpa.copy().acceptDepthFirst(treeVisitor)
                            yield()

                            layout.build(treeVisitor.root)
                            yield()

                            lastFilter = filter
                            lastFilterEpa = filteredEpa
                            lastLayoutConfig = layoutConfig
                            lastLayout = layout
                            layout
                        }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            layout = layout,
                            statistics = null,
                        )
                    }
                } catch (e: CancellationException) {
                    logger.warn { "Cancellation Exception ${e.message}" }
                } catch (e: Exception) {
                    // optional: handle error (show in UI, log, etc.)
                    logger.error { "Error building layout: ${e.message}" }
                    _uiState.update {
                        it.copy(isLoading = false, layout = null, statistics = null)
                    }
                }
            }
        }
    }
}

@Composable
fun EpaTreeViewUi(
    epa: ExtendedPrefixAutomata<Long>,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    onClose: () -> Unit,
) {
    val viewModel =
        remember {
            EpaViewModel(
                completeEpa = epa,
                backgroundDispatcher = backgroundDispatcher,
            )
        }

    val uiState by viewModel.uiState.collectAsState()

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
                LayoutOptionUi {
                    viewModel.updateLayout(it)
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
                FilterUi(epa = epa, onApply = {
                    viewModel.updateFilter(it)
                })
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
                    TidyTreeUi(uiState)
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

    val tabs = listOf("Activity", "State Frequency", "Partition Frequency", "Chain Pruning")
    var selectedIndex by remember { mutableStateOf(0) }
    var stateFrequencyThreashold by remember { mutableStateOf(100f) }
    var partitionFrequencyThreashold by remember { mutableStateOf(100f) }

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
                    val activityFilter = ActivityFilter<Long>(selectedActivities.toHashSet())

                    val combined = PartitionFrequencyFilter<Long>(partitionFrequencyThreashold / 100f)
//                        activityFilter
//                            .then(
//                                StateFrequencyFilter(stateFrequencyThreashold / 100f),
//                            ).then(
//                                PartitionFrequencyFilter(partitionFrequencyThreashold / 100f),
//                            )

                    onApply(combined)
                },
                modifier = Modifier.height(48.dp),
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Abort")
                Spacer(Modifier.width(8.dp))
                Text("Apply", color = Color.White, style = MaterialTheme.typography.button)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ScrollableTabRow(selectedTabIndex = selectedIndex, backgroundColor = Color.White) {
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
            1 -> {
                Column {
                    // State Frequency
                    Text("Frequency $stateFrequencyThreashold")
                    Slider(
                        value = stateFrequencyThreashold,
                        onValueChange = { stateFrequencyThreashold = it },
                        valueRange = 0.0f..100f,
                    )
                    LazyColumn {
                        items(epa.states.toList()) {
//                            Text("${it.name}: ${frequencyStateVisitor.frequencyByState(it)}")
                        }
                    }
                }
            }
            2 -> {
                Column {
                    // State Frequency

                    val frequencyParitionVisitor = NormalizedPartitionFrequencyVisitor<Long>()
                    epa.copy().acceptDepthFirst(frequencyParitionVisitor)

                    Text("Frequency $partitionFrequencyThreashold")
                    Slider(
                        value = partitionFrequencyThreashold,
                        onValueChange = { partitionFrequencyThreashold = it },
                        valueRange = 0.0f..100f,
                    )
                    LazyColumn {
                        items(epa.getAllPartitions().sorted()) {
                            Text("$it: ${frequencyParitionVisitor.frequencyByPartition(it)}")
                        }
                    }
                }
            }
            else -> {
                Text("TODO: implement ${tabs[selectedIndex]}")
            }
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
fun LayoutOptionUi(onUpdate: (LayoutConfig) -> Unit) {
    var showLayoutOptions by remember { mutableStateOf(false) }
    var radius by remember { mutableStateOf(120.0f) }
    var margin by remember { mutableStateOf(3.0f) }
    val layouts: List<LayoutSelection> =
        listOf(
            LayoutSelection("Walker Radial Tree"),
            LayoutSelection("Walker"),
            LayoutSelection(
                "Direct Angular Placement",
            ),
        )

    var layoutSelection by remember { mutableStateOf(layouts.first()) }

    Button(
        shape = RoundedCornerShape(24.dp),
        onClick = { showLayoutOptions = !showLayoutOptions },
        modifier = Modifier.height(48.dp),
    ) {
        Icon(Icons.Default.Close, contentDescription = "Close")
    }

    Column(horizontalAlignment = Alignment.End) {
        if (showLayoutOptions) {
            LayoutOptions(
                radius = radius,
                onRadiusChange = {
                    radius = it
                    onUpdate(
                        LayoutConfig(
                            radius = radius,
                            margin = margin.degreesToRadians(),
                            layout = layoutSelection,
                        ),
                    )
                },
                margin = margin,
                onMarginChange = {
                    margin = it
                    onUpdate(
                        LayoutConfig(
                            radius = radius,
                            margin = margin.degreesToRadians(),
                            layout = layoutSelection,
                        ),
                    )
                },
                layouts = layouts,
                onLayoutSelectionChange = {
                    layoutSelection = it
                    logger.info { "setting layout to $it" }
                    onUpdate(
                        LayoutConfig(
                            radius = radius,
                            margin = margin.degreesToRadians(),
                            layout = layoutSelection,
                        ),
                    )
                },
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

@Composable
fun LayoutOptions(
    radius: Float,
    onRadiusChange: (Float) -> Unit,
    margin: Float,
    onMarginChange: (Float) -> Unit,
    layouts: List<LayoutSelection>,
    onLayoutSelectionChange: (LayoutSelection) -> Unit,
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
