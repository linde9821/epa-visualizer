package moritz.lindner.masterarbeit.playground

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2018ChallengeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2020
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EpaFromXesBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.xes.Sepsis
import moritz.lindner.masterarbeit.epa.features.filter.PartitionFrequencyFilter
import moritz.lindner.masterarbeit.epa.features.statistics.StatesAndPartitionsByDepthVisitor
import java.io.File

fun main() {
    val logger = KotlinLogging.logger {}


    val challenge2017Offer2017 =
        File("./data/eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to BPI2017OfferChallengeEventMapper()
    val challenge2017 = File("./data/eventlogs/BPI Challenge 2017.xes.gz") to BPI2017ChallengeEventMapper()
    val challenge2018 = File("./data/eventlogs/BPI Challenge 2018.xes.gz") to BPI2018ChallengeMapper()
    val challenge2020Internationale = File("./data/eventlogs/InternationalDeclarations.xes.gz") to BPI2020()
    val sepsis = File("./data/eventlogs/Sepsis Cases - Event Log.xes.gz") to Sepsis()

    val logs = listOf(
        challenge2017Offer2017,
        challenge2017,
        challenge2018,
        challenge2020Internationale,
        sepsis
    )

    val callback = EpaProgressCallback { current, total, task ->
        if (current % 100 == 0L) {
            logger.info {  "$task: $current/$total" }
        }
    }

    csvWriter().open("/Users/moritzlindner/programming/masterarbeit/epa-visualizer/data/statistics/filter_analysis.csv") {
        writeRow("log", "states", "events", "states-after-0.05", "event-after-0.05", "states-after-0.005", "event-after-0.005", "states-after-0.0005", "event-after-0.0005")

        logs.forEach { (file, mapper) ->
            val epa =
                EpaFromXesBuilder<Long>()
                    .setFile(file)
                    .setEventLogMapper(mapper)
                    .setProgressCallback(callback)
                    .build()

            val epaService = EpaService<Long>()

            val filterSmall = PartitionFrequencyFilter<Long>(0.05f) // 5%
            val filterSmallest = PartitionFrequencyFilter<Long>(0.005f) // 0.5%
            val extraSmall = PartitionFrequencyFilter<Long>(0.0005f) // 0.05%

            val smallEpa = epaService.applyFilters(epa.copy(), listOf(filterSmall))
            val smallestEpa = epaService.applyFilters(epa.copy(), listOf(filterSmallest))
            val extraEpa = epaService.applyFilters(epa.copy(), listOf(extraSmall))

            writeRow(
                mapper.name,
                epa.states.size,
                epa.states.sumOf { epa.sequence(it).size },

                smallEpa.states.size,
                smallEpa.states.sumOf { smallEpa.sequence(it).count() },

                smallestEpa.states.size,
                smallestEpa.states.sumOf { smallestEpa.sequence(it).count() },

                extraEpa.states.size,
                extraEpa.states.sumOf { extraEpa.sequence(it).count() },
            )
        }
    }
}
