package moritz.lindner.masterarbeit.ui.components.epaview.state.manager

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.XESEventLogMapper
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutFactory
import moritz.lindner.masterarbeit.epa.features.lod.LODQuery
import moritz.lindner.masterarbeit.epa.features.lod.NoLOD
import moritz.lindner.masterarbeit.epa.features.lod.steiner.SteinerTreeLOD
import moritz.lindner.masterarbeit.epa.features.lod.steiner.SteinerTreeLODBuilder
import moritz.lindner.masterarbeit.epa.features.statistics.Statistics
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.EpaLayoutCanvasRenderer
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DefaultConfig
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DrawAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.highlight.HighlightingAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.labels.StateLabels
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.maxScale
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.minScale
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.rememberCanvasState
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.TabState
import moritz.lindner.masterarbeit.ui.components.epaview.state.TaskProgressState
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.skia.Color
import kotlin.coroutines.cancellation.CancellationException

@Suppress("UNCHECKED_CAST")
class EpaStateManager(
    private val tabStateManager: TabStateManager,
    private val backgroundDispatcher: ExecutorCoroutineDispatcher,
    projectStateManager: ProjectStateManager
) {
    private val epaService = EpaService<Long>()
    private val scope = CoroutineScope(backgroundDispatcher + SupervisorJob())

    private val _epaByTabId = MutableStateFlow<Map<String, ExtendedPrefixAutomaton<Long>>>(emptyMap())
    val epaByTabId = _epaByTabId.asStateFlow()

    private val _stateLabelsByTabId = MutableStateFlow<Map<String, StateLabels>>(emptyMap())
    val stateLabelsByTabId = _stateLabelsByTabId.asStateFlow()

    private val _highlightingByTabId = MutableStateFlow<Map<String, HighlightingAtlas>>(emptyMap())
    val highlightingByTabId = _highlightingByTabId.asStateFlow()

    private val _drawAtlasByTabId = MutableStateFlow<Map<String, DrawAtlas>>(emptyMap())
    val drawAtlasByTabId = _drawAtlasByTabId.asStateFlow()

    private val _lodByTabId = MutableStateFlow<Map<String, LODQuery>>(emptyMap())
    val lodByTabId = _lodByTabId.asStateFlow()

    private val _layoutAndConfigByTabId = MutableStateFlow<Map<String, Pair<Layout, LayoutConfig>>>(emptyMap())
    val layoutAndConfigByTabId = _layoutAndConfigByTabId.asStateFlow()

    private val _statisticsByTabId = MutableStateFlow<Map<String, Statistics<Long>>>(emptyMap())
    val statisticsByTabId = _statisticsByTabId.asStateFlow()

    private val projectFlow = projectStateManager.project

    private val _animationState = MutableStateFlow(AnimationState.Empty)
    val animationState = _animationState.asStateFlow()

    private val _progressByTabId = MutableStateFlow<Map<String, TaskProgressState?>>(emptyMap())
    val progressByTabId = _progressByTabId.asStateFlow()

    val windowManager = WindowManager()

    fun updateAnimation(animationState: AnimationState) {
        _animationState.value = animationState
    }

    fun openEpaInNewWindow(
        tabId: String,
    ) {
        val treeLayout = _layoutAndConfigByTabId.value[tabId]?.first
        val tabState = tabStateManager.getActiveTab()
        val stateLabels = _stateLabelsByTabId.value[tabId]
        val drawAtlas = _drawAtlasByTabId.value[tabId]
        val highlightingAtlas = _highlightingByTabId.value[tabId]

        if (
            treeLayout != null &&
            tabState != null &&
            stateLabels != null &&
            drawAtlas != null &&
            highlightingAtlas != null
        ) {
            windowManager.openWindow(
                title = "EPA read only view of ${tabState.title}",
                windowState = WindowState(
                    width = 800.dp,
                    height = 800.dp,
                    position = WindowPosition(Alignment.Center)
                )
            ) { _ ->
                EpaLayoutCanvasRenderer(
                    layout = treeLayout,
                    stateLabels = stateLabels,
                    drawAtlas = drawAtlas,
                    onStateHover = {},
                    onRightClickState = {},
                    onLeftClickState = {},
                    tabState = tabState,
                    highlightingAtlas = highlightingAtlas,
                    animationState = _animationState.value,
                    canvasState = rememberCanvasState(),
                    lodQuery = _lodByTabId.value[tabId] ?: NoLOD(),
                    backgroundDispatcher = backgroundDispatcher
                )
            }
        }
    }

    fun removeAllForTab(tabId: String) {
        _epaByTabId.update { currentMap ->
            currentMap.filterNot { it.key == tabId }
        }
        _stateLabelsByTabId.update { currentMap ->
            currentMap.filterNot { it.key == tabId }
        }
        _layoutAndConfigByTabId.update { currentMap ->
            currentMap.filterNot { it.key == tabId }
        }
        _statisticsByTabId.update { currentMap ->
            currentMap.filterNot { it.key == tabId }
        }
        _drawAtlasByTabId.update { currentMap ->
            currentMap.filterNot { it.key == tabId }
        }
        _lodByTabId.update { currentMap ->
            currentMap.filterNot { it.key == tabId }
        }
        _progressByTabId.update { currentMap ->
            currentMap.filterNot { it.key == tabId }
        }
    }

    private fun invalidateAllEpas() {
        _epaByTabId.value = emptyMap()
        _stateLabelsByTabId.value = emptyMap()
        _layoutAndConfigByTabId.value = emptyMap()
        _statisticsByTabId.value = emptyMap()
        _drawAtlasByTabId.value = emptyMap()
        _lodByTabId.value = emptyMap()
        _progressByTabId.value = emptyMap()
    }

    init {
        var rebuildJob: Job? = null

        scope.launch {
            projectFlow
                .map { project -> project.getMapper() as? XESEventLogMapper<Long> }
                .distinctUntilChanged()
                .drop(1)
                .collect { _ ->
                    // Cancel any in-progress rebuilding
                    rebuildJob?.cancel()

                    // Invalidate immediately
                    invalidateAllEpas()

                    // Start new rebuild job
                    rebuildJob = scope.launch(backgroundDispatcher) {
                        try {
                            tabStateManager.tabs.value.forEach { tab ->
                                buildEpaForTab(tab)
                                buildLayoutAndDrawAtlasForTab(tab)
                                buildStateLabelsForTab(tab)
                                buildStatisticForTab(tab)
                                buildHighlightingForTab(tab)
                            }
                        } catch (e: CancellationException) {
                            logger.info { "Rebuild cancelled (mapper changed)" }
                        } catch (e: Exception) {
                            logger.error(e) { "Error while building state" }
                        }
                    }
                }
        }

        scope.launch {
            tabStateManager
                .tabs
                .collectLatest { tabs ->
                    logger.info { "Collecting latest tabs" }
                    for (tab in tabs) {
                        try {
                            buildEpaForTab(tab)
                            ensureActive()
                            launch {
                                try {
                                    buildLayoutAndDrawAtlasForTab(tab)
                                    ensureActive()
                                    buildStateLabelsForTab(tab)
                                    ensureActive()
                                    buildStatisticForTab(tab)
                                    ensureActive()
                                    buildHighlightingForTab(tab)
                                }catch (e: Exception) {
                                    logger.error(e) { "Tab state building failed" }
                                }
                            }
                        } catch (e: CancellationException) {
                            logger.warn(e) { "canceling current tabs building" }
                        } catch (e: Exception) {
                            // TODO: move try catch into functions and set error for tabs accordingly
                            logger.error(e) { "Error while building state" }
                        }
                    }

                }
        }
    }

    private fun buildLodForTab(tabState: TabState, config: LayoutConfig) {
        logger.info { "build lods" }

        val lod = if (config.lod) {
            val epa = _epaByTabId.value[tabState.id]!!
            val lodBuilder = SteinerTreeLODBuilder(epa)
            val lods = lodBuilder.buildLODLevels()

            SteinerTreeLOD<Long>(
                lodLevels = lods,
                minScale = minScale,
                maxScale = maxScale,
            )
        } else {
            NoLOD()
        }

        logger.info { "build lods completed" }

        _lodByTabId.update { currentMap ->
            currentMap + (tabState.id to lod)
        }
    }

    fun highlightPathFromRootForState(tabId: String, state: State) {
        val highlight = _highlightingByTabId.value[tabId]!!
        val pathFromRoot = epaService.getPathFromRoot(state)
        val newHighlight = highlight.withHighlightedState(pathFromRoot.toSet())
        _highlightingByTabId.update { currentMap ->
            currentMap + (tabId to newHighlight)
        }
    }

    fun buildHighlightingForTab(tabState: TabState) {
        if (_highlightingByTabId.value.containsKey(tabState.id)) {
            return
        }

        _highlightingByTabId.update { currentMap ->
            currentMap + (tabState.id to HighlightingAtlas(
                selectedState = tabState.selectedState
            ))
        }
    }

    fun buildStatisticForTab(tabState: TabState) {
        if (_statisticsByTabId.value.containsKey(tabState.id)) {
            return
        }

        val epa = _epaByTabId.value[tabState.id]!!
        val statistics = epaService.getStatistics(epa)

        _statisticsByTabId.update { currentMap ->
            currentMap + (tabState.id to statistics)
        }
    }

    fun buildStateLabelsForTab(
        tabState: TabState
    ) {
        if (_stateLabelsByTabId.value.containsKey(tabState.id)) {
            return
        }

        val epa = _epaByTabId.value[tabState.id]!!

        val stateLabels = StateLabels(
            backgroundColor = Color.WHITE,
            baseFontSize = 21f,
        )

        val states = epa.states
        val chunkSize = 100

        runBlocking {
            states.chunked(chunkSize).forEach { chunk ->
                chunk.map { state ->
                    scope.async { stateLabels.generateLabelForState(state) }
                }.awaitAll()
            }
        }

        _stateLabelsByTabId.update { currentMap ->
            currentMap + (tabState.id to stateLabels)
        }
    }

    fun buildLayoutAndDrawAtlasForTab(
        tabState: TabState
    ) {
        val layoutAndConfig = _layoutAndConfigByTabId.value[tabState.id]

        if (layoutAndConfig?.second == tabState.layoutConfig) {
            return
        }

        val progressCallback = EpaProgressCallback { current, total, task ->
            updateProgress(
                tabId = tabState.id,
                current = current,
                total = total,
                task = task
            )
        }

        val epa = _epaByTabId.value[tabState.id]!!
        val updatedLayout = try {
            LayoutFactory.createLayout(
                config = tabState.layoutConfig,
                extendedPrefixAutomaton = epa,
                backgroundDispatcher = backgroundDispatcher,
                progressCallback = progressCallback
            ).also { it.build(progressCallback) }
        } catch (e: Exception) {
            updateProgress(
                tabId = tabState.id,
                current = 0,
                total = 1,
                task = "Error while constructing layout (please check your parameters): ${e.message}"
            )
            throw e
        }

        _layoutAndConfigByTabId.update { currentMap ->
            currentMap + (tabState.id to (updatedLayout to tabState.layoutConfig))
        }
        buildLodForTab(tabState, tabState.layoutConfig)

        logger.info { "building atlas" }

        val atlas = DrawAtlas.build(
            epa,
            DefaultConfig(
                extendedPrefixAutomaton = epa,
                stateSize = tabState.layoutConfig.stateSize,
                minTransitionSize = tabState.layoutConfig.minTransitionSize,
                maxTransitionSize = tabState.layoutConfig.maxTransitionSize,
                colorPalette = tabState.layoutConfig.colorPalette,
                progressCallback = progressCallback
            ),
            stateSizeUntilLabelIsDrawn = tabState.layoutConfig.stateSizeUntilLabelIsDrawn,
            transitionDrawMode = tabState.layoutConfig.transitionDrawMode,
            progressCallback = progressCallback,
        )
        clearProgress(tabState.id)
        _drawAtlasByTabId.update { currentMap ->
            currentMap + (tabState.id to atlas)
        }
        logger.info { "atlas build" }

        clearProgress(tabState.id)
    }

    fun buildEpaForTab(
        tabState: TabState,
    ) {
        if (_epaByTabId.value.containsKey(tabState.id)) {
            return
        }

        try {
            // Set initial progress
            updateProgress(
                tabId = tabState.id,
                current = 0,
                total = 1,
                task = "Initializing EPA generation"
            )

            // Create progress callback
            val progressCallback = EpaProgressCallback { current, total, task ->
                updateProgress(
                    tabId = tabState.id,
                    current = current,
                    total = total,
                    task = task
                )
            }

            // Build the EPA with progress tracking
            val builder = EpaFromXesBuilder<Long>()
                .setFile(projectFlow.value.getXesFilePath().toFile())
                .setEventLogMapper(projectFlow.value.getMapper() as XESEventLogMapper<Long>)
                .setProgressCallback(progressCallback)

            val originalEpa = builder.build()
            val filteredEpa = epaService.applyFilters(originalEpa, tabState.filters, progressCallback)

            // Store the generated EPA
            _epaByTabId.update { currentMap ->
                currentMap + (tabState.id to filteredEpa)
            }

            // Clear progress when complete
            clearProgress(tabState.id)
        } catch (e: Exception) {
            // Handle error - update progress with error state
            updateProgress(
                tabId = tabState.id,
                current = 0,
                total = 1,
                task = "Error: ${e.message}"
            )
        }
    }

    fun updateProgress(
        tabId: String,
        current: Long,
        total: Long,
        task: String
    ) {
        _progressByTabId.update { currentProgressMap ->
            currentProgressMap.plus(
                tabId to TaskProgressState(
                    current = current,
                    total = total,
                    taskName = task
                )
            )
        }
    }

    fun clearProgress(tabId: String) {
        _progressByTabId.update { currentProgressMap ->
            currentProgressMap.plus(
                tabId to null
            )
        }
    }
}