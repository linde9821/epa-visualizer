package moritz.lindner.masterarbeit.ui.components.epaview.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutFactory
import moritz.lindner.masterarbeit.epa.features.layout.tree.EpaToTree
import moritz.lindner.masterarbeit.epa.features.statistics.StatisticsVisitor
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaUiState
import moritz.lindner.masterarbeit.ui.components.epaview.state.StatisticsState
import moritz.lindner.masterarbeit.ui.logger
import kotlin.coroutines.cancellation.CancellationException

class EpaViewModel(
    val completeEpa: ExtendedPrefixAutomaton<Long>,
    val backgroundDispatcher: ExecutorCoroutineDispatcher,
) {

    fun updateFilters(filters: List<EpaFilter<Long>>) {
        _Epa_uiState.update {
            it.copy(
                filters = filters
            )
        }
    }

    fun updateLayout(layoutConfig: LayoutConfig) {
        _layout.value = layoutConfig
    }

    fun updateAnimation(animationState: AnimationState) {
        _animationState.value = animationState
    }

    private val _layout: MutableStateFlow<LayoutConfig> = MutableStateFlow(LayoutConfig.RadialWalker())
    val layout: StateFlow<LayoutConfig> = _layout

    private val _Epa_uiState =
        MutableStateFlow(
            EpaUiState(
                null,
                true,
                completeEpa,
                emptyList()
            ),
        )
    val epaUiState: StateFlow<EpaUiState> = _Epa_uiState.asStateFlow()

    private val _animationState = MutableStateFlow(AnimationState.Companion.Empty)
    val animationState = _animationState.asStateFlow()

    private val coroutineScope = CoroutineScope(backgroundDispatcher + SupervisorJob())

    private val _statistics = MutableStateFlow<StatisticsState<Long>?>(null)
    val statistics: StateFlow<StatisticsState<Long>?> = _statistics.asStateFlow()

    init {
        coroutineScope.launch {
            val layoutFlow = _layout.distinctUntilChanged { a, b ->
                a == b
            }
            val filtersFlow = _Epa_uiState
                .map { uiState -> uiState.filters }
                .distinctUntilChanged()

            // Track previous values to detect what changed
            var previousLayout: LayoutConfig? = null
            var previousFilters: List<EpaFilter<Long>>? = null
            var currentFilteredEpa: ExtendedPrefixAutomaton<Long>? = null

            combine(layoutFlow, filtersFlow) { layout, filters ->
                layout to filters
            }.collectLatest { (layoutConfig, filters) ->
                logger.info { "running state update" }

                _Epa_uiState.update { uiState -> uiState.copy(isLoading = true) }

                try {
                    withContext(backgroundDispatcher) {
                        if (filters != previousFilters) {
                            launch {
                                computeStatistics(currentFilteredEpa)
                            }
                        } else {
                            logger.info { "Skipping statistics computation (data unchanged)" }
                        }

                        if (filters != previousFilters) {
                            currentFilteredEpa = applyFilter(filters)
                            previousFilters = filters
                            yield()
                            _Epa_uiState.update { uiState -> uiState.copy(filteredEpa = currentFilteredEpa) }
                        } else {
                            logger.info { "Skipping filter application (filters unchanged)" }
                        }

                        val shouldRebuildLayout = layoutConfig != previousLayout || filters != previousFilters
                        if (shouldRebuildLayout && currentFilteredEpa != null) {
                            launch {
                                buildTree(currentFilteredEpa, layoutConfig)
                                previousLayout = layoutConfig
                            }
                        } else {
                            logger.info { "Skipping layout rebuild (layout unchanged and no new data)" }
                            _Epa_uiState.update { it.copy(isLoading = false) }
                        }
                    }
                } catch (e: CancellationException) {
                    logger.warn { "Cancellation Exception ${e.message}" }
                } catch (e: Exception) {
                    logger.error { "Error building layout: ${e.message}" }
                    _Epa_uiState.update {
                        it.copy(isLoading = false, layout = null, filteredEpa = null)
                    }
                }
            }
        }
    }

    private suspend fun buildTree(
        filteredEpa: ExtendedPrefixAutomaton<Long>,
        layoutConfig: LayoutConfig
    ) {
        logger.info { "building tree" }
        val treeVisitor = EpaToTree<Long>()
        filteredEpa.copy().acceptDepthFirst(treeVisitor)
        yield()

        logger.info { "building tree layout" }
        val layout = LayoutFactory.create(layoutConfig)
        yield()

        // TODO: ensure that it works even when epa is empty
        layout.build(treeVisitor.root)
        yield()

        logger.info { "update ui" }

        _Epa_uiState.update { uiState ->
            uiState.copy(
                isLoading = false,
                layout = layout,
                filteredEpa = filteredEpa
            )
        }
    }

    private fun applyFilter(filters: List<EpaFilter<Long>>): ExtendedPrefixAutomaton<Long> {
        logger.info { "applying filters" }
        return filters.fold(completeEpa) { epa, filter ->
            filter.apply(epa)
        }
    }

    private suspend fun computeStatistics(filteredEpa: ExtendedPrefixAutomaton<Long>?) {
        withContext(backgroundDispatcher) {
            logger.info { "building statistics" }
            _statistics.update {
                null
            }

            val fullVisitor = StatisticsVisitor<Long>()
            val statistics1 = async {
                completeEpa.acceptDepthFirst(fullVisitor)
            }

            val filterVisitor = StatisticsVisitor<Long>()
            val statistics2 = async {
                filteredEpa?.acceptDepthFirst(filterVisitor)
            }

            yield()

            awaitAll(statistics1, statistics2)
            _statistics.update {
                StatisticsState(
                    fullEpa = fullVisitor.build(),
                    filteredEpa = filteredEpa?.let { filterVisitor.build() },
                )
            }
        }
    }
}