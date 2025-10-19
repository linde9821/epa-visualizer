package moritz.lindner.masterarbeit.ui.components.epaview.state.manager

import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.api.LayoutService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EventLogMapper
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.layout.Layout
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.statistics.Statistics
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.DetailComparison
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DefaultConfig
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas.DrawAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.highlight.HighlightingAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.labels.StateLabels
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.TabState
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.skia.Color
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

data class ManagedWindow(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val windowState: WindowState = WindowState(),
    val content: @Composable (ManagedWindow) -> Unit
)

class WindowManager {
    private val _windows = MutableStateFlow<List<ManagedWindow>>(emptyList())
    val windows = _windows.asStateFlow()

    fun openWindow(
        title: String,
        windowState: WindowState = WindowState(),
        content: @Composable (ManagedWindow) -> Unit
    ): String {
        val window = ManagedWindow(
            title = title,
            windowState = windowState,
            content = content
        )
        _windows.update { it + window }
        return window.id
    }

    fun closeWindow(windowId: String) {
        _windows.update { it.filterNot { window -> window.id == windowId } }
    }

    fun closeWindow(window: ManagedWindow) {
        closeWindow(window.id)
    }

    fun updateWindowTitle(windowId: String, newTitle: String) {
        _windows.update { windows ->
            windows.map { window ->
                if (window.id == windowId) window.copy(title = newTitle)
                else window
            }
        }
    }
}

class EpaStateManager(
    private val tabStateManager: TabStateManager,
    private val backgroundDispatcher: ExecutorCoroutineDispatcher,
    projectStateManager: ProjectStateManager
) {
    private val epaService = EpaService<Long>()
    private val layoutService = LayoutService<Long>()
    private val scope = CoroutineScope(backgroundDispatcher + SupervisorJob())

    private val _epaByTabId = MutableStateFlow<Map<String, ExtendedPrefixAutomaton<Long>>>(emptyMap())
    val epaByTabId = _epaByTabId.asStateFlow()

    private val _stateLabelsByTabId = MutableStateFlow<Map<String, StateLabels>>(emptyMap())
    val stateLabelsByTabId = _stateLabelsByTabId.asStateFlow()

    private val _highlightingByTabId = MutableStateFlow<Map<String, HighlightingAtlas>>(emptyMap())
    val highlightingByTabId = _highlightingByTabId.asStateFlow()

    private val _drawAtlasByTabId = MutableStateFlow<Map<String, DrawAtlas>>(emptyMap())
    val drawAtlasByTabId = _drawAtlasByTabId.asStateFlow()

    private val _layoutAndConfigByTabId = MutableStateFlow<Map<String, Pair<Layout, LayoutConfig>>>(emptyMap())
    val layoutAndConfigByTabId = _layoutAndConfigByTabId.asStateFlow()

    private val _statisticsByTabId = MutableStateFlow<Map<String, Statistics<Long>>>(emptyMap())
    val statisticsByTabId = _statisticsByTabId.asStateFlow()

    private val projectFlow = projectStateManager.project

    private val _animationState = MutableStateFlow(AnimationState.Empty)
    val animationState = _animationState.asStateFlow()

    val windowManager = WindowManager()

    fun updateAnimation(animationState: AnimationState) {
        _animationState.value = animationState
    }

    fun openStateComparisonWindow(
        extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
        drawAtlas: DrawAtlas,
        primaryState: State,
        secondaryState: State,
    ) {
        windowManager.openWindow(
            title = "State Comparison ${primaryState.name} -> ${secondaryState.name}",
            windowState = WindowState(
                width = 800.dp,
                height = 600.dp,
                position = WindowPosition(Alignment.Center)
            )
        ) { window ->
            val subEpa = epaService.buildSubEpa(extendedPrefixAutomaton, listOf(primaryState, secondaryState))

            val tree = LayoutService<Long>().buildLayout(subEpa, LayoutConfig.Walker())

//            DetailComparison(tree, drawAtlas)
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
    }

    private fun invalidateAllEpas() {
        _epaByTabId.value = emptyMap()
        _stateLabelsByTabId.value = emptyMap()
        _layoutAndConfigByTabId.value = emptyMap()
        _statisticsByTabId.value = emptyMap()
        _drawAtlasByTabId.value = emptyMap()
    }

    init {
        var rebuildJob: Job? = null

        scope.launch {
            projectFlow
                .map { it.getMapper() as? EventLogMapper<Long> }
                .distinctUntilChanged()
                .drop(1) // Skip initial value
                .collect { newMapper ->
                    // Cancel any in-progress rebuilding
                    rebuildJob?.cancel()

                    // Invalidate immediately
                    invalidateAllEpas()

                    // Start new rebuild job
                    rebuildJob = scope.launch(backgroundDispatcher) {
                        try {
                            tabStateManager.tabs.value.forEach { tab ->
                                // Check if still active
                                ensureActive()
                                buildEpaForTab(tab)
                                buildLayoutForTab(tab)
                                buildStateLabelsForTab(tab)
                                buildDrawAtlasForTab(tab)
                                buildStatisticForTab(tab)
                                buildHighlightingForTab(tab)
                            }
                        } catch (e: CancellationException) {
                            logger.info { "Rebuild cancelled (mapper changed)" }
                            throw e
                        } catch (e: Exception) {
                            logger.error(e) { "Error while building state" }
                        }
                    }
                }
        }

        scope.launch {
            tabStateManager.tabs.collect { tabs ->
                try {
                    tabs.forEach { tab ->
                        // build epa
                        buildEpaForTab(tab)
                        // build layout
                        buildLayoutForTab(tab)
                        // build labels
                        buildStateLabelsForTab(tab)
                        // build draw atlas
                        buildDrawAtlasForTab(tab)
                        // build statistics
                        buildStatisticForTab(tab)
                        // build highlighting
                        buildHighlightingForTab(tab)
                    }
                } catch (e: Exception) {
                    // TODO: move try catch into functions and set error for tab
                    logger.error(e) { "Error while building state" }
                }
            }
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

    fun setSelectedState(tabId: String, selectedState: State) {
        val highlight = _highlightingByTabId.value[tabId]!!
        val newHighlight = highlight.selectedState(selectedState)
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

    suspend fun buildStateLabelsForTab(
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

        states.chunked(chunkSize).forEach { chunk ->
            chunk.map { state ->
                scope.async { stateLabels.generateLabelForState(state) }
            }.awaitAll()
        }

        _stateLabelsByTabId.update { currentMap ->
            currentMap + (tabState.id to stateLabels)
        }
    }

    fun buildDrawAtlasForTab(
        tabState: TabState
    ) {
        val drawAtlas = _drawAtlasByTabId.value[tabState.id]
        if (drawAtlas != null) return

        val epa = _epaByTabId.value[tabState.id]!!

        logger.info { "building atlas" }

        val progressCallback = EpaProgressCallback { current, total, task ->
            tabStateManager.updateProgress(
                tabId = tabState.id,
                current = current,
                total = total,
                task = task
            )
        }

        val atlas = DrawAtlas.build(
            epa,
            DefaultConfig(
                extendedPrefixAutomaton = epa,
                stateSize = 25f,
                minTransitionSize = 2f,
                maxTransitionSize = 25f,
                progressCallback = progressCallback
            ),
            progressCallback = progressCallback
        )
        tabStateManager.clearProgress(tabState.id)
        _drawAtlasByTabId.update { currentMap ->
            currentMap + (tabState.id to atlas)
        }
    }

    fun buildLayoutForTab(
        tabState: TabState
    ) {
        val layoutAndConfig = _layoutAndConfigByTabId.value[tabState.id]
        if (layoutAndConfig?.second == tabState.layoutConfig) {
            return
        }

        val progressCallback = EpaProgressCallback { current, total, task ->
            tabStateManager.updateProgress(
                tabId = tabState.id,
                current = current,
                total = total,
                task = task
            )
        }

        val epa = _epaByTabId.value[tabState.id]!!
        val layout = layoutService.buildLayout(epa, tabState.layoutConfig, progressCallback)
        _layoutAndConfigByTabId.update { currentMap ->
            currentMap + (tabState.id to (layout to tabState.layoutConfig))
        }
        tabStateManager.clearProgress(tabState.id)
    }

    fun buildEpaForTab(
        tabState: TabState,
    ) {
        if (_epaByTabId.value.containsKey(tabState.id)) {
            return
        }

        try {
            // Set initial progress
            tabStateManager.updateProgress(
                tabId = tabState.id,
                current = 0,
                total = 1,
                task = "Initializing EPA generation"
            )

            // Create progress callback
            val progressCallback = EpaProgressCallback { current, total, task ->
                tabStateManager.updateProgress(
                    tabId = tabState.id,
                    current = current,
                    total = total,
                    task = task
                )
            }

            // Build the EPA with progress tracking
            val builder = EpaFromXesBuilder<Long>()
                .setFile(projectFlow.value.getXesFilePath().toFile())
                .setEventLogMapper(projectFlow.value.getMapper() as EventLogMapper<Long>)
                .setProgressCallback(progressCallback)

            val originalEpa = builder.build()
            val filteredEpa = epaService.applyFilters(originalEpa, tabState.filters, progressCallback)

            // Store the generated EPA
            _epaByTabId.update { currentMap ->
                currentMap + (tabState.id to filteredEpa)
            }

            // Clear progress when complete
            tabStateManager.clearProgress(tabState.id)
        } catch (e: Exception) {
            // Handle error - update progress with error state
            tabStateManager.updateProgress(
                tabId = tabState.id,
                current = 0,
                total = 1,
                task = "Error: ${e.message}"
            )
        }
    }
}