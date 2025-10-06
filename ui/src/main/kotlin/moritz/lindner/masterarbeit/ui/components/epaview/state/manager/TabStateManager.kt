package moritz.lindner.masterarbeit.ui.components.epaview.state.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.ui.components.epaview.state.TabState
import moritz.lindner.masterarbeit.ui.components.epaview.state.TaskProgressState

/** Main state class managing all tabs */
class TabStateManager {
    private val _tabs = MutableStateFlow<List<TabState>>(emptyList())
    val tabs: StateFlow<List<TabState>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String?>(null)
    val activeTabId: StateFlow<String?> = _activeTabId.asStateFlow()

    /** Add a new tab */
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

    fun setSelectedStateForCurrentTab(selectedState: State) {
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.id == activeTabId.value) {
                    tab.copy(selectedState = selectedState)
                } else {
                    tab
                }
            }
        }
    }

    /** Remove a tab by ID */
    fun removeTab(tabId: String) {
        _tabs.update { currentTabs ->
            currentTabs.filterNot { it.id == tabId }
        }

        // If removed tab was active, activate another tab
        if (_activeTabId.value == tabId) {
            _activeTabId.value = _tabs.value.firstOrNull()?.id
        }
    }

    /** Set the active tab */
    fun setActiveTab(tabId: String) {
        _activeTabId.value = tabId
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                tab.copy(isActive = tab.id == tabId)
            }
        }
    }

    /** Update progress for a specific tab */
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
                        progress = TaskProgressState(
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

    /** Clear progress for a specific tab */
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

    /** Get a specific tab by ID */
    fun getTab(tabId: String): TabState? {
        return _tabs.value.find { it.id == tabId }
    }

    /** Get the currently active tab */
    fun getActiveTab(): TabState? {
        return _activeTabId.value?.let { activeId ->
            _tabs.value.find { it.id == activeId }
        }
    }
}