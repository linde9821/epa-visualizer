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
import moritz.lindner.masterarbeit.epa.visitor.statistics.StatisticsVisitor
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
    private val coroutineScope = CoroutineScope(backgroundDispatcher + SupervisorJob())

    private val _statistics = MutableStateFlow<StatisticsState?>(null)
    val statistics: StateFlow<StatisticsState?> = _statistics.asStateFlow()

    init {
        coroutineScope.launch {
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
                logger.info { "layout: $layoutConfig" }
                _uiState.update { it.copy(isLoading = true) }

                try {
                    withContext(backgroundDispatcher) {
                        val filteredEpa = filter.apply(completeEpa.copy())
                        yield()
                        _uiState.update { it.copy(filteredEpa = filteredEpa) }

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
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                layout = layout,
                            )
                        }

                        computeStatistics(filteredEpa)
                    }
                } catch (e: CancellationException) {
                    logger.warn { "Cancellation Exception ${e.message}" }
                } catch (e: Exception) {
                    logger.error { "Error building layout: ${e.message}" }
                    _uiState.update {
                        it.copy(isLoading = false, layout = null, filteredEpa = null)
                    }
                }
            }
        }
    }

    private suspend fun computeStatistics(filteredEpa: ExtendedPrefixAutomata<Long>?) {
        withContext(backgroundDispatcher) {
            val fullVisitor = StatisticsVisitor<Long>()
            completeEpa.acceptDepthFirst(fullVisitor)

            val filterVisitor = StatisticsVisitor<Long>()
            filteredEpa?.acceptDepthFirst(filterVisitor)

            _statistics.value =
                StatisticsState(
                    fullEpa = fullVisitor.build(),
                    filteredEpa = filteredEpa?.let { filterVisitor.build() },
                )
        }
    }
}
