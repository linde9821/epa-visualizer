package moritz.lindner.masterarbeit.ui.components.treeview.state

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
import moritz.lindner.masterarbeit.epa.drawing.layout.TreeLayout
import moritz.lindner.masterarbeit.epa.drawing.tree.TreeBuildingVisitor
import moritz.lindner.masterarbeit.epa.filter.DoNothingFilter
import moritz.lindner.masterarbeit.epa.filter.EpaFilter
import moritz.lindner.masterarbeit.ui.components.logger
import moritz.lindner.masterarbeit.ui.components.treeview.layout.LayoutConfig
import moritz.lindner.masterarbeit.ui.components.treeview.layout.LayoutSelection
import moritz.lindner.masterarbeit.ui.components.treeview.layout.TreeLayoutConstructionHelper
import kotlin.coroutines.cancellation.CancellationException

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
                5f,
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
                    logger.error { "Error building layout: ${e.message}" }
                    _uiState.update {
                        it.copy(isLoading = false, layout = null, statistics = null)
                    }
                }
            }
        }
    }
}
