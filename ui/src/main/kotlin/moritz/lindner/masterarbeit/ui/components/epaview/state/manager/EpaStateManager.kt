package moritz.lindner.masterarbeit.ui.components.epaview.state.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import moritz.lindner.masterarbeit.ui.components.epaview.components.tree.StateLabels
import moritz.lindner.masterarbeit.ui.components.epaview.state.TabState
import org.jetbrains.skia.Color

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

    private val _layoutAndConfigByTabId = MutableStateFlow<Map<String, Pair<TreeLayout, LayoutConfig>>>(emptyMap())
    val layoutAndConfigByTabId = _layoutAndConfigByTabId.asStateFlow()

    private val projectFlow = projectStateManager.project

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
    }

    init {
        scope.launch {
            tabStateManager.tabs.collect { tabs ->
                tabs.forEach { tab ->
                    // build epa
                    buildEpaForTab(tab)

                    // build layout
                    buildLayoutForTab(tab)

                    // build labels
                    buildStateLabelsForTab(tab)
                }
            }
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