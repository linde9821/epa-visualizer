package moritz.lindner.masterarbeit.metrics.scenario

import kotlinx.coroutines.asCoroutineDispatcher
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2018ChallengeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2020
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.Sepsis
import moritz.lindner.masterarbeit.epa.construction.builder.xes.XESEventLogMapper
import moritz.lindner.masterarbeit.epa.features.filter.CompressionFilter
import moritz.lindner.masterarbeit.epa.features.filter.PartitionFrequencyFilter
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutConfig
import moritz.lindner.masterarbeit.epa.features.layout.factory.LayoutFactory
import org.jetbrains.kotlinx.dataframe.api.aggregate
import org.jetbrains.kotlinx.dataframe.api.colsOf
import org.jetbrains.kotlinx.dataframe.api.convert
import org.jetbrains.kotlinx.dataframe.api.groupBy
import org.jetbrains.kotlinx.dataframe.api.max
import org.jetbrains.kotlinx.dataframe.api.mean
import org.jetbrains.kotlinx.dataframe.api.print
import org.jetbrains.kotlinx.dataframe.api.rename
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.jetbrains.kotlinx.dataframe.api.with
import org.jetbrains.kotlinx.dataframe.io.writeCSV
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.measureTimedValue

// Data class to securely hold the typed measurements before aggregation
data class ScenarioResult(
    val eventLog: String,
    val initialEpaCreationMs: Duration,
    val initialLayoutMs: Duration,
    val applyingFiltersMs: Duration,
    val secondLayoutMs: Duration,
    val totalScenarioTimeMs: Duration,
    val eventLogSize: Int,
    val statesSize: Int
)

fun main() {
    val rootPath = System.getProperty("project.root") ?: "."
    val repoRoot = File(rootPath)
    val outputFile = File(repoRoot, "/data/results/scenario/scenario.csv")
    outputFile.parentFile.mkdirs()

    val challenge2017Offer2017 =
        File(repoRoot, "/data/eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to BPI2017OfferChallengeEventMapper()
    val challenge2017 = File(repoRoot, "/data/eventlogs/BPI Challenge 2017.xes.gz") to BPI2017ChallengeEventMapper()
    val challenge2018 = File(repoRoot, "/data/eventlogs/BPI Challenge 2018.xes.gz") to BPI2018ChallengeMapper()
    val challenge2020Internationale = File(repoRoot, "/data/eventlogs/InternationalDeclarations.xes.gz") to BPI2020()
    val sepsis = File(repoRoot, "/data/eventlogs/Sepsis Cases - Event Log.xes") to Sepsis()

    val logs = listOf(
        sepsis,
        challenge2020Internationale,
        challenge2017Offer2017,
        challenge2017,
        challenge2018,
    )

    val warmupIteration = 6
    val measurementIteration = 4

    println("Starting JVM Warmup...")
    // 1. Deep Warmup: Run multiple times so the JIT compiler fully optimizes the hot paths.
    repeat(warmupIteration) { iteration ->
        println("Warmup iteration ${iteration + 1}/$warmupIteration")
        logs.forEach { log ->
            runScenario(log)
        }
    }

    println("Starting Measurement...")
    val results = mutableListOf<ScenarioResult>()

    logs.forEach { log ->
        // 2. Multiple Measurements per log
        repeat(measurementIteration) {
            // Hint to the JVM to clean up memory before the next run to avoid mid-run GC pauses
            System.gc()
            Thread.sleep(100) // Give the JVM a moment to settle

            results.add(runScenario(log))
        }
    }

    // 3. Convert to DataFrame and aggregate using the mean
    val aggregatedDf = results.toDataFrame()
        .groupBy("eventLog")
        .aggregate {
            mean("initialEpaCreationMs") into "initial EPA creation"
            mean("initialLayoutMs") into "initial Layout construction"
            mean("applyingFiltersMs") into "Applying Filters"
            mean("secondLayoutMs") into "Second Layout Construction"
            mean("totalScenarioTimeMs") into "total scenario time"

            // Using max() bypasses the compiler confusion with first()
            // Since the sizes are identical across iterations, max == first
            max("eventLogSize") into "Event Log Size"
            max("statesSize") into "States Size"
        }
        .rename("eventLog" to "Event Log")

    // 4. Format the aggregated Double millisecond values back to your preferred String format.
    // colsOf<Double>() automatically selects all time columns, meaning we don't have to list strings or cast!
    val formattedDf = aggregatedDf
        .convert { colsOf<Double>() }
        .with { it.formattedSecondsMillis() }

    // 5. Output the results
    formattedDf.print()
    formattedDf.writeCSV(outputFile.absolutePath)
    println("Results saved to ${outputFile.absolutePath}")
}

fun runScenario(log: Pair<File, XESEventLogMapper<Long>>): ScenarioResult {
    val (file, mapper) = log
    val epaService = EpaService<Long>()

    // 1. create epa
    val (epa, step1) = measureTimedValue {
        EpaFromXesBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()
    }

    val (_, step2) = measureTimedValue {
        val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        val createLayout = LayoutFactory.createLayout(
            config = LayoutConfig.RadialWalkerConfig(),
            extendedPrefixAutomaton = epa,
            backgroundDispatcher = executor,
        )
        createLayout.build()
    }

    val (epaFiltered, step3) = measureTimedValue {
        epaService.applyFilters(
            epa, listOf(
                PartitionFrequencyFilter(0.05f),
                CompressionFilter()
            )
        )
    }

    val (_, step4) = measureTimedValue {
        val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        val createLayout = LayoutFactory.createLayout(
            config = LayoutConfig.RadialWalkerConfig(),
            extendedPrefixAutomaton = epaFiltered,
            backgroundDispatcher = executor,
        )
        createLayout.build()
    }

    val events = epa.states.sumOf { epa.sequence(it).size }

    // Return the strongly-typed data class instead of strings
    return ScenarioResult(
        eventLog = mapper.name,
        initialEpaCreationMs = step1,
        initialLayoutMs = step2,
        applyingFiltersMs = step3,
        secondLayoutMs = step4,
        totalScenarioTimeMs = (step1 + step2 + step3 + step4),
        eventLogSize = events,
        statesSize = epa.states.size
    )
}

fun Double.formattedSecondsMillis(): String {
    val seconds = this / 1000.0
    return String.format(Locale.US, "%.4f", seconds)
}