package moritz.lindner.masterarbeit.metrics.filter

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2018ChallengeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2020
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.Sepsis
import moritz.lindner.masterarbeit.epa.construction.builder.xes.XESEventLogMapper
import moritz.lindner.masterarbeit.epa.features.filter.EpaFilter
import moritz.lindner.masterarbeit.epa.features.filter.PartitionFrequencyFilter
import moritz.lindner.masterarbeit.epa.features.filter.StateFrequencyFilter
import java.io.File
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.math.pow
import kotlin.time.measureTime

@OptIn(ExperimentalAtomicApi::class)
fun main() {
    val rootPath = System.getProperty("project.root") ?: "."
    val repoRoot = File(rootPath)
    val processors = (Runtime.getRuntime().availableProcessors() / 1.5).toInt()
    val logger = KotlinLogging.logger {}

    val challenge2017Offer2017 =
        File(repoRoot, "/data/eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to BPI2017OfferChallengeEventMapper()
    val challenge2017 = File(repoRoot, "/data/eventlogs/BPI Challenge 2017.xes.gz") to BPI2017ChallengeEventMapper()
    val challenge2018 = File(repoRoot, "/data/eventlogs/BPI Challenge 2018.xes.gz") to BPI2018ChallengeMapper()
    val challenge2020Internationale = File(repoRoot, "/data/eventlogs/InternationalDeclarations.xes.gz") to BPI2020()
    val sepsis = File(repoRoot, "/data/eventlogs/Sepsis Cases - Event Log.xes.gz") to Sepsis()

    val logs = listOf(
        challenge2017Offer2017,
        challenge2017,
        challenge2018,
        challenge2020Internationale,
        sepsis
    )

    val epaService = EpaService<Long>()
    val n = 10_000
    val maxP = 0.15
    val stateFilters = List(n) {
        val p = (it.toDouble() / n.toDouble()).pow(3.0) * maxP
        StateFrequencyFilter<Long>(p.toFloat()) to p.toFloat()
    }
    val partitionFilters = List(n) {
        val p = (it.toDouble() / n.toDouble()).pow(3.0) * maxP
        PartitionFrequencyFilter<Long>(p.toFloat()) to p.toFloat()
    }

    runFilterReport(logs, repoRoot, stateFilters, processors, logger, epaService)

    runFilterReport(logs, repoRoot, partitionFilters, processors, logger, epaService)
}

@OptIn(ExperimentalAtomicApi::class)
private fun runFilterReport(
    logs: List<Pair<File, XESEventLogMapper<Long>>>,
    repoRoot: File,
    filters: List<Pair<EpaFilter<Long>, Float>>,
    processors: Int,
    logger: KLogger,
    epaService: EpaService<Long>,
) {
    logs.forEach { (file, mapper) ->
        val outputFile = File(
            repoRoot,
            "/data/statistics/filter/filter_analysis_complete_${mapper.name.trim()}_${filters.first().first.name}.csv"
        )
        outputFile.parentFile.mkdirs()
        csvWriter().open(outputFile) {
            writeRow(
                "log",
                "total_states",
                "total_events",
                "filter name",
                "filter setting",
                "events after filter",
                "states after filter",
                "events p%"
            )

            val epa = EpaFromXesBuilder<Long>()
                .setFile(file)
                .setEventLogMapper(mapper)
                .build()

            val totalEventCount = epa.states.sumOf { epa.sequence(it).size }
            val progressCounter = AtomicInt(0)

            // Limit concurrent executions to the number of processors
            val semaphore = Semaphore(processors)

            measureTime {
                val reportRows = runBlocking {
                    logger.info { "Starting analysis for ${file.name} with $processors workers..." }
                    val reports = filters.map { filter ->
                        async(Dispatchers.Default) {
                            semaphore.withPermit {
                                val filteredEpa = epaService.applyFilters(epa, listOf(filter.first))
                                val filteredEventCount = filteredEpa.states.sumOf { filteredEpa.sequence(it).count() }

                                FilterReport(
                                    logName = mapper.name,
                                    totalStates = epa.states.size,
                                    totalEvents = totalEventCount,
                                    filterName = filter.first.name,
                                    filterThreshold = filter.second,
                                    eventsAfterFilter = filteredEventCount,
                                    statesAfterFilter = filteredEpa.states.size,
                                    eventsPercentage = filteredEventCount.toFloat() / totalEventCount
                                ).also {
                                    val current = progressCounter.incrementAndFetch()
                                    if (current % 100 == 0) {
                                        logger.info { "Processed $current / ${filters.size} filters" }
                                    }
                                }
                            }
                        }
                    }.awaitAll()

                    logger.info { "Sorting and writing ${reports.size} results..." }

                    reports
                        .sortedBy(FilterReport::filterThreshold)
                        .map(FilterReport::toRow)
                }
                logger.info { "writing rows ${reportRows.size}" }
                writeRows(reportRows)
            }.also { duration ->
                logger.info { "Execution of ${mapper.name} took $duration" }
            }
        }
    }
}