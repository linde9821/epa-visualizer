package moritz.lindner.masterarbeit.ui.components.epaview.state.manager

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
import kotlinx.coroutines.withContext
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.api.LayoutService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EventLogMapper
import moritz.lindner.masterarbeit.epa.features.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.statistics.Statistics
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.StateLabels
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.DefaultConfig
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.DrawAtlas
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.TabState
import moritz.lindner.masterarbeit.ui.logger
import org.jetbrains.skia.Color
import kotlin.coroutines.cancellation.CancellationException

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

    private val _drawAtlasByTabId = MutableStateFlow<Map<String, DrawAtlas>>(emptyMap())
    val drawAtlasByTabId = _drawAtlasByTabId.asStateFlow()

    private val _layoutAndConfigByTabId = MutableStateFlow<Map<String, Pair<TreeLayout, LayoutConfig>>>(emptyMap())
    val layoutAndConfigByTabId = _layoutAndConfigByTabId.asStateFlow()

    private val _statisticsByTabId = MutableStateFlow<Map<String, Statistics<Long>>>(emptyMap())
    val statisticsByTabId = _statisticsByTabId.asStateFlow()

    private val projectFlow = projectStateManager.project

    private val _animationState = MutableStateFlow(AnimationState.Empty)
    val animationState = _animationState.asStateFlow()

    fun updateAnimation(animationState: AnimationState) {
        _animationState.value = animationState
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
                    }
                } catch (e: Exception) {
                    // TODO: move try catch into functions and set error for tab
                    logger.error(e) { "Error while building state" }
                }

            }
        }
    }

    fun buildStatisticForTab(
        tabState: TabState
    ) {
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

        withContext(backgroundDispatcher) {
            states.chunked(chunkSize).forEachIndexed { chunkIndex, chunk ->
                chunk.map { state ->
                    scope.async { stateLabels.generateLabelForState(state) }
                }.awaitAll()
            }
        }

        _stateLabelsByTabId.update { currentMap ->
            currentMap + (tabState.id to stateLabels)
        }
    }

    suspend fun buildDrawAtlasForTab(
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
                stateSize = 15f,
                minTransitionSize = 2f,
                maxTransitionSize = 25f,
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

        val epa = _epaByTabId.value[tabState.id]!!
        val layout = layoutService.buildLayout(epa, tabState.layoutConfig)
        _layoutAndConfigByTabId.update { currentMap ->
            currentMap + (tabState.id to (layout to tabState.layoutConfig))
        }
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