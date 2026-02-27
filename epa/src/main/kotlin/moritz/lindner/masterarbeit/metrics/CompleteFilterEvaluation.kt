package moritz.lindner.masterarbeit.metrics

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
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
import moritz.lindner.masterarbeit.epa.features.filter.PartitionFrequencyFilter
import moritz.lindner.masterarbeit.epa.features.filter.StateFrequencyFilter
import java.io.File
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.math.pow
import kotlin.time.measureTime

data class FilterReport(
    val logName: String,
    val totalStates: Int,
    val totalEvents: Int,
    val filterName: String,
    val filterThreshold: Float, // or Float, depending on your precision needs
    val eventsAfterFilter: Int,
    val statesAfterFilter: Int,
    val eventsPercentage: Float
) {
    fun toRow(): List<String> = listOf(
        logName,
        totalStates.toString(),
        totalEvents.toString(),
        filterName,
        filterThreshold.toString(),
        eventsAfterFilter.toString(),
        statesAfterFilter.toString(),
        // Formats percentage to 2 decimal places (e.g., "85.24")
        "%.2f".format(eventsPercentage)
    )
}

@OptIn(ExperimentalAtomicApi::class)
fun main() {
    val rootPath = System.getProperty("project.root") ?: "."
    val repoRoot = File(rootPath)
    val processors = (Runtime.getRuntime().availableProcessors() / 1.5).toInt()
    // Dispatchers.Default is already optimized for CPU-bound tasks
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

    val n = 4000
    val maxP = 0.10
    val filters = List(n) {
        // This creates very dense samples near 0 and spreads out toward 10%
        val p = (it.toDouble() / n.toDouble()).pow(2.0) * maxP
        StateFrequencyFilter<Long>(p.toFloat())
    }

    val epaService = EpaService<Long>()

    logs.forEach { (file, mapper) ->
        val outputFile = File(repoRoot, "/data/statistics/filter/filter_analysis_complete_${mapper.name.trim()}_${filters.first().name}.csv")
        outputFile.parentFile.mkdirs()
        csvWriter().open(outputFile) {
            // Use a static header or a helper from your data class
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
                val rows = runBlocking {
                    logger.info { "Starting analysis for ${file.name} with $processors workers..." }
                    val reports = filters.map { filter ->
                        async(Dispatchers.Default) {
                            semaphore.withPermit {
                                val filteredEpa = epaService.applyFilters(epa, listOf(filter))
                                val filteredEventCount = filteredEpa.states.sumOf { filteredEpa.sequence(it).count() }

                                val report = FilterReport(
                                    logName = mapper.name,
                                    totalStates = epa.states.size,
                                    totalEvents = totalEventCount,
                                    filterName = filter.name,
                                    filterThreshold = filter.threshold,
                                    eventsAfterFilter = filteredEventCount,
                                    statesAfterFilter = filteredEpa.states.size,
                                    eventsPercentage = filteredEventCount.toFloat() / totalEventCount
                                )

                                val current = progressCounter.incrementAndFetch()
                                if (current % 500 == 0) {
                                    logger.info { "Processed $current / ${filters.size} filters" }
                                }

                                report
                            }
                        }
                    }.awaitAll()

                    logger.info { "Sorting and writing ${reports.size} results..." }

                    reports
                        .sortedBy { it.filterThreshold }
                        .map { it.toRow() }
                }
                logger.info { "writing rows ${rows.size}" }
                writeRows(rows)
            }.also {
                logger.info { "Execution of ${mapper.name} took $it" }
            }

        }
    }
}