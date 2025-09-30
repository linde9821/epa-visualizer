package moritz.lindner.masterarbeit.ui.components.epaview.components

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EventLogMapper
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.project.Project

/**
 * Represents the progress of a task within a tab
 */
data class TaskProgress(
    val current: Long = 0,
    val total: Long = 0,
    val taskName: String = ""
) {
    val percentage: Float
        get() = if (total > 0) (current.toFloat() / total.toFloat()) else 0f

    val isComplete: Boolean
        get() = total in 1..current
}

/**
 * Represents a single tab with metadata and progress
 */
data class TabState(
    val id: String,
    val title: String,
    val isActive: Boolean = false,
    val progress: TaskProgress? = null,
    val filters: List<EpaFilter<Long>> = emptyList(),
    val layoutConfig: LayoutConfig
)

/**
 * Main state class managing all tabs
 */
class TabStateManager {
    private val _tabs = MutableStateFlow<List<TabState>>(emptyList())
    val tabs: StateFlow<List<TabState>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String?>(null)
    val activeTabId: StateFlow<String?> = _activeTabId.asStateFlow()

    /**
     * Add a new tab
     */
    fun addTab(
        id: String,
        title: String,
        filters: List<EpaFilter<Long>>,
        layoutConfig: LayoutConfig
    ) {
        _tabs.update { currentTabs ->
            if (currentTabs.any { it.id == id }) {
                currentTabs // Tab already exists
            } else {
                currentTabs + TabState(
                    id = id,
                    title = title,
                    filters = filters,
                    layoutConfig = layoutConfig
                )
            }
        }

        // If this is the first tab, make it active
        if (_activeTabId.value == null) {
            setActiveTab(id)
        }
    }

    fun updateLayout(tabId: String, layoutConfig: LayoutConfig) {
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.id == tabId) {
                    tab.copy(layoutConfig = layoutConfig)
                } else {
                    tab
                }
            }
        }
    }

    /**
     * Remove a tab by ID
     */
    fun removeTab(tabId: String) {
        _tabs.update { currentTabs ->
            currentTabs.filterNot { it.id == tabId }
        }

        // If removed tab was active, activate another tab
        if (_activeTabId.value == tabId) {
            _activeTabId.value = _tabs.value.firstOrNull()?.id
        }
    }

    /**
     * Set the active tab
     */
    fun setActiveTab(tabId: String) {
        _activeTabId.value = tabId
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                tab.copy(isActive = tab.id == tabId)
            }
        }
    }

    /**
     * Update progress for a specific tab
     */
    fun updateProgress(
        tabId: String,
        current: Long,
        total: Long,
        task: String
    ) {
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.id == tabId) {
                    tab.copy(
                        progress = TaskProgress(
                            current = current,
                            total = total,
                            taskName = task
                        )
                    )
                } else {
                    tab
                }
            }
        }
    }

    /**
     * Clear progress for a specific tab
     */
    fun clearProgress(tabId: String) {
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.id == tabId) {
                    tab.copy(progress = null)
                } else {
                    tab
                }
            }
        }
    }

    /**
     * Get a specific tab by ID
     */
    fun getTab(tabId: String): TabState? {
        return _tabs.value.find { it.id == tabId }
    }

    /**
     * Get the currently active tab
     */
    fun getActiveTab(): TabState? {
        return _activeTabId.value?.let { activeId ->
            _tabs.value.find { it.id == activeId }
        }
    }
}

class ProjectStateManager(
    project: Project,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
) {
    private val _project = MutableStateFlow<Project>(project)
    val project = _project.asStateFlow()

    fun updateProject(project: Project) {
        project.saveMetadata()
        _project.value = project

        // TODO: invalidate existing epas if necessary
    }
}

class EpaStateManager(
    private val tabStateManager: TabStateManager,
    private val backgroundDispatcher: ExecutorCoroutineDispatcher,
    projectStateManager: ProjectStateManager
) {
    private val epaService = EpaService<Long>()
    private val scope = CoroutineScope(backgroundDispatcher + SupervisorJob())
    private val _epaByTabId = MutableStateFlow<Map<String, ExtendedPrefixAutomaton<Long>>>(emptyMap())
    val epaByTabId = _epaByTabId.asStateFlow()

    private val projectFlow = projectStateManager.project

    fun removeEpaForTab(tabId: String) {
        _epaByTabId.update { currentMap ->
            currentMap.filterNot { it.key == tabId }
        }
    }

    fun buildEpaForTab(
        tabId: String,
    ) {
        if (_epaByTabId.value.containsKey(tabId)) {
            return
        }

        val tabState = tabStateManager.getTab(tabId) ?: return

        scope.launch(backgroundDispatcher) {
            try {
                // Set initial progress
                tabStateManager.updateProgress(
                    tabId = tabId,
                    current = 0,
                    total = 1,
                    task = "Initializing EPA generation"
                )

                // Create progress callback
                val progressCallback = EpaProgressCallback { current, total, task ->
                    tabStateManager.updateProgress(
                        tabId = tabId,
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
                    currentMap + (tabId to filteredEpa)
                }

                // Clear progress when complete
                tabStateManager.clearProgress(tabId)

            } catch (e: Exception) {
                // Handle error - update progress with error state
                tabStateManager.updateProgress(
                    tabId = tabId,
                    current = 0,
                    total = 1,
                    task = "Error: ${e.message}"
                )
            }
        }
    }
}