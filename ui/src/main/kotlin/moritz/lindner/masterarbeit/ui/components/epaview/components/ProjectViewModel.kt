package moritz.lindner.masterarbeit.ui.components.epaview.components

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EventLogMapper
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.project.Project
import java.util.UUID

data class EpaViewConfig(
    val id: UUID = UUID.randomUUID(),
    val filters: List<EpaFilter<Long>>,
    val layoutConfig: LayoutConfig,
    val parent: EpaViewConfig?,
    val name: String,
    internal val children: MutableList<EpaViewConfig> = mutableListOf()
)

data class EpaTab(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val configId: UUID
)

data class TabProgress(
    val current: Long = 0,
    val total: Long = 0,
    val task: String = "",
    val isActive: Boolean = false
) {
    fun progress(): Float {
        return if (total > 0) {
            current.toFloat() / total.toFloat()
        } else {
            0f
        }
    }
}

class ProjectViewModel(
    project: Project,
    backgroundDispatcher: ExecutorCoroutineDispatcher,
    rootConfig: EpaViewConfig
) {
    private val coroutineScope = CoroutineScope(backgroundDispatcher + SupervisorJob())
    private val epaService = EpaService<Long>()

    private val _projectState = MutableStateFlow(project)
    val projectState: StateFlow<Project> = _projectState.asStateFlow()

    private val _rootConfig = MutableStateFlow<EpaViewConfig>(rootConfig)
    val rootConfig = _rootConfig.asStateFlow()

    // Map from config ID to EPA
    private val _epaByConfigId = MutableStateFlow<Map<UUID, ExtendedPrefixAutomaton<Long>>>(emptyMap())
    val epaByConfigId: StateFlow<Map<UUID, ExtendedPrefixAutomaton<Long>>> = _epaByConfigId.asStateFlow()

    // Tab management
    private val _tabsByConfigId = MutableStateFlow<Map<UUID, EpaTab>>(emptyMap())
    val tabsByConfigId = _tabsByConfigId.asStateFlow()

    private val _tabProgressByConfigId = MutableStateFlow<Map<UUID, TabProgress>>(emptyMap())
    val tabProgressByConfigId: StateFlow<Map<UUID, TabProgress>> = _tabProgressByConfigId.asStateFlow()

    // Active tab
    val _activeTab = MutableStateFlow<EpaTab>(null)
    val activeTab: StateFlow<EpaTab> = _tabsByConfigId.combine(_activeTabId) { tabs, activeId ->
        tabs[activeId]!!
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = null
    )

    // Active config (config of the active tab)
    val activeConfig: StateFlow<EpaViewConfig?> = activeTab.map { tab ->
        tab?.let { findConfigById(it.configId) }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = null
    )

    val activeEpa: StateFlow<ExtendedPrefixAutomaton<Long>?> = activeConfig.combine(_epaByConfigId) { config, epaMap ->
        config?.let { epaMap[it.id] }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = null
    )

    fun updateProject(project: Project) {
        project.saveMetadata()
        _projectState.value = project

        // TODO: invalidate existing epas if necessary
    }

    fun createTab(title: String, configId: UUID): EpaTab {
        val tab = EpaTab(
            title = title,
            configId = configId
        )
        _tabsByConfigId.value += tab
        _activeTabId.value = tab.id

        // Initialize progress for this tab
        _tabProgressByConfigId.value += (tab.id to TabProgress())

        return tab
    }

    fun buildRoot() {
        val rootConfig = EpaViewConfig(
            filters = emptyList(),
            layoutConfig = LayoutConfig.RadialWalker(),
            parent = null,
            name = "Root"
        )

        _rootConfig.value = rootConfig

        val tab = createTab("Root", rootConfig.id)

        // Create progress callback for this tab
        val tabCallback = createProgressCallback(tab.id)

        // Build EPA with combined callback
        val builder = EpaFromXesBuilder<Long>()
            .setFile(_projectState.value.getXesFilePath().toFile())
            .setEventLogMapper(_projectState.value.getMapper() as EventLogMapper<Long>)
            .setProgressCallback(tabCallback)
        val epa = builder.build()

        _epaByConfigId.value = mapOf(rootConfig.id to epa)

        // Clear progress when done
        clearProgress(tab.id)
    }

    fun createProgressCallback(tabId: UUID): EpaProgressCallback {
        return EpaProgressCallback { current, total, task ->
            _tabProgressByConfigId.value += (tabId to TabProgress(
                current = current,
                total = total,
                task = task,
                isActive = true
            ))
        }
    }

    fun clearProgress(tabId: UUID) {
        _tabProgressByConfigId.value += (tabId to TabProgress(isActive = false))
    }

    fun applyNewFilters(
        parent: EpaViewConfig,
        newFilters: List<EpaFilter<Long>>,
    ) {
        coroutineScope.launch {
            // Combine parent filters with new filters
            val allFilters = parent.filters + newFilters

            // Get parent EPA
            val parentEpa = _epaByConfigId.value[parent.id]
                ?: throw IllegalStateException("Parent EPA not found")

            // Create child config
            val childConfig = EpaViewConfig(
                filters = allFilters,
                layoutConfig = parent.layoutConfig,
                parent = parent,
                name = epaService.filterNames(parent.filters + newFilters)
            )

            // Add to parent's children
            parent.children.add(childConfig)

            // Trigger recomposition
            _rootConfig.value = _rootConfig.value

            // Create tab and get progress callback
            val tab = createTab(childConfig.name, childConfig.id)
            val progressCallback = createProgressCallback(tab.id)

            // Apply filters and build EPA
            try {
                val filteredEpa = epaService.applyFilters(parentEpa, childConfig.filters, progressCallback)
                // Store EPA
                _epaByConfigId.value += (childConfig.id to filteredEpa)

            } catch (e: Exception) {
                // Handle error - maybe remove the config and tab
                parent.children.remove(childConfig)
            } finally {
                clearProgress(tab.id)
            }
        }
    }
}