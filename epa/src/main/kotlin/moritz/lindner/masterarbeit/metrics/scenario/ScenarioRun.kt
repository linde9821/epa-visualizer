package moritz.lindner.masterarbeit.metrics.scenario

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
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
import java.io.File
import java.util.concurrent.Executors
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.measureTimedValue

@OptIn(ExperimentalAtomicApi::class)
fun main() {
    val rootPath = System.getProperty("project.root") ?: "."
    val repoRoot = File(rootPath)
    val outputFile = File(
        repoRoot,
        "/data/statistics/scenario/scenario.csv"
    )
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

    // jvm warmup
    logs.forEach { log ->
        runScenario(log)
    }

    csvWriter().open(outputFile) {
        writeRow(
            "Event Log",
            "initial EPA creation",
            "initial Layout construction",
            "Applying Filters",
            "Second Layout Construction",
            "total scenario time",
            "Event Log Size",
            "States Size",
        )

        logs.forEach { log ->
            val row = runScenario(log)
            writeRow(row)
        }
    }
}

fun runScenario(log: Pair<File, XESEventLogMapper<Long>>): List<String> {
    val (file, mapper) = log
    val epaService = EpaService<Long>()

    // 1. create epa
    val (epa, step1) = measureTimedValue {
        EpaFromXesBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()
    }

    // 2. create layout
    val (_, step2) = measureTimedValue {
        val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        val createLayout = LayoutFactory.createLayout(
            config = LayoutConfig.RadialWalkerConfig(),
            extendedPrefixAutomaton = epa,
            backgroundDispatcher = executor,
        )
        createLayout.build()
    }

    // 3. apply filters
    val (epaFiltered, step3) = measureTimedValue {
        epaService.applyFilters(
            epa, listOf(
                PartitionFrequencyFilter<Long>(0.05f),
                CompressionFilter<Long>()
            )
        )
    }

    // 4. create layout for filtered epa
    val (_, step4) = measureTimedValue {
        val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        val createLayout = LayoutFactory.createLayout(
            config = LayoutConfig.RadialWalkerConfig(),
            extendedPrefixAutomaton = epaFiltered,
            backgroundDispatcher = executor,
        )
        createLayout.build()
    }

    return buildList {
        add(mapper.name)
        val events = epa.states.sumOf { epa.sequence(it).size }
        add(step1.formattedSecondsMillis())
        add(step2.formattedSecondsMillis())
        add(step3.formattedSecondsMillis())
        add(step4.formattedSecondsMillis())
        add((step1 + step2 + step3 + step4).formattedSecondsMillis())
        add(events.toString())
        add(epa.states.size.toString())
    }
}

fun Duration.formattedSecondsMillis(): String {
    return toComponents { seconds, nanoseconds ->
        val millis = nanoseconds / 1_000_000
        "$seconds.${millis.toString().padStart(3, '0')}"
    }
}

