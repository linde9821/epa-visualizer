package moritz.lindner.masterarbeit.playground

import io.github.oshai.kotlinlogging.KotlinLogging
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.construction.builder.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.BPI2018ChallangeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomatonBuilder
import moritz.lindner.masterarbeit.epa.construction.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.domain.Event
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.features.statistics.NormalizedPartitionFrequencyVisitor
import moritz.lindner.masterarbeit.epa.visitor.AutomatonVisitor
import java.io.File

fun main() {
    val logger = KotlinLogging.logger {}

    val sample = File("./data/eventlogs/sample.xes") to SampleEventMapper()
    val sample2 = File("./data/eventlogs/sample2.xes") to SampleEventMapper()
    val loops = File("./data/eventlogs/loops.xes") to SampleEventMapper()
    val challenge2017Offers =
        File("./data/eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to BPI2017OfferChallengeEventMapper()
    val challenge2017 =
        File("./data/eventlogs/BPI Challenge 2017.xes.gz") to BPI2017ChallengeEventMapper()
    val challenge2018 = File("./epa/src/main/resources/eventlogs/BPI Challenge 2018.xes.gz") to BPI2018ChallangeMapper()

    val (file, mapper) = sample

    logger.info { "Parsing ${file.absolutePath}" }

    val epa =
        ExtendedPrefixAutomatonBuilder<Long>()
            .setFile(file)
            .setEventLogMapper(mapper)
            .build()

    logger.info { "build EPA successfully" }

    val paritionFrequencyFilter = NormalizedPartitionFrequencyVisitor<Long>()

    epa.acceptDepthFirst(paritionFrequencyFilter)

    val topPartitions = paritionFrequencyFilter.getTopN(2).map { it.key!! }.toSet()

    epa.acceptDepthFirst(object : AutomatonVisitor<Long> {

        val sequenceByPartition = HashMap<Int, List<Event<Long>>>()

        override fun visit(
            extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
            state: State,
            depth: Int
        ) {
            val partition = extendedPrefixAutomaton.partition(state)
            if (topPartitions.contains(partition)) {
                val sequence = extendedPrefixAutomaton
                    .sequence(state)
                    .sortedBy { it.timestamp }
                if (sequence.last().successorIndex == null) {
                    sequenceByPartition[partition] = sequence
                }
            }
        }

        override fun onEnd(extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>) {
            println("Sequence by partition")
            sequenceByPartition.forEach { (partition, sequence) ->
                println("Partition $partition")
                sequence.forEach { event ->
                    println(event)
                }
                println()
            }
        }
    })
}
