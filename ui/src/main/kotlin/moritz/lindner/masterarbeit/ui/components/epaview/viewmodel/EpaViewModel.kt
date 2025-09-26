package moritz.lindner.masterarbeit.ui.components.epaview.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
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
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.api.LayoutService
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.ui.components.epaview.state.AnimationState
import moritz.lindner.masterarbeit.ui.components.epaview.state.EpaUiState
import moritz.lindner.masterarbeit.ui.components.epaview.state.StatisticsState
import moritz.lindner.masterarbeit.ui.logger
import kotlin.coroutines.cancellation.CancellationException

class EpaViewModel(
    val completeEpa: ExtendedPrefixAutomaton<Long>,
    val backgroundDispatcher: ExecutorCoroutineDispatcher,
) {

    private val epaService = EpaService<Long>()
    private val layoutService = LayoutService<Long>()

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

    private val _animationState = MutableStateFlow(AnimationState.Empty)
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
            var previousFilters: List<EpaFilter<Long>> = emptyList()
            var currentFilteredEpa: ExtendedPrefixAutomaton<Long>? = null

            combine(layoutFlow, filtersFlow) { layout, filters ->
                layout to filters
            }.collectLatest { (layoutConfig, newFilters) ->
                logger.info { "running state update" }

                _Epa_uiState.update { uiState -> uiState.copy(isLoading = true) }

                try {
                    withContext(backgroundDispatcher) {
                        val shouldRebuildLayout = layoutConfig != previousLayout || newFilters != previousFilters

                        if (newFilters != previousFilters || currentFilteredEpa == null) {
                            logger.info { "applying filters" }
                            currentFilteredEpa = epaService.applyFilters(completeEpa, newFilters)
                            computeStatistics(currentFilteredEpa)
                            previousFilters = newFilters
                            yield()

                            _Epa_uiState.update { uiState -> uiState.copy(filteredEpa = currentFilteredEpa) }
                        } else {
                            logger.info { "Skipping filter application (filters unchanged)" }
                        }

                        if (shouldRebuildLayout) {
                            launch {
                                val layout = layoutService.buildLayout(currentFilteredEpa, layoutConfig)
                                yield()
                                previousLayout = layoutConfig
                                _Epa_uiState.update { uiState ->
                                    uiState.copy(
                                        isLoading = false,
                                        layout = layout,
                                    )
                                }
                            }
                        } else {
                            logger.info { "Skipping layout rebuild (layout unchanged and no new data)" }
                            _Epa_uiState.update { it.copy(isLoading = false) }
                        }
                    }
                } catch (e: CancellationException) {
                    logger.warn(e) { "Cancellation Exception ${e.message}" }
                } catch (e: Exception) {
                    logger.error(e) { "Error building layout: ${e.message}" }
                    _Epa_uiState.update {
                        it.copy(isLoading = false, layout = null, filteredEpa = null)
                    }
                }
                logger.info { "finished state update" }
            }
        }
    }

    private suspend fun computeStatistics(filteredEpa: ExtendedPrefixAutomaton<Long>?) {
        withContext(backgroundDispatcher) {
            logger.info { "building statistics" }

            if (_statistics.value == null) {
                val fullStatistics = epaService.getStatistics(completeEpa)
                _statistics.update { _ ->
                    StatisticsState(
                        fullEpa = fullStatistics,
                        filteredEpa = null
                    )
                }
            }

            if (filteredEpa != null) {
                val filterStatistics = epaService.getStatistics(filteredEpa)

                _statistics.update { statisticsState ->
                    statisticsState?.copy(
                        filteredEpa = filterStatistics
                    )
                }
            }
        }
    }
}
