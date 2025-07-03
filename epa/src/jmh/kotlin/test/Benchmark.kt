package test

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.builder.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.BPI2018ChallangeMapper
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.SampleEventMapper
import moritz.lindner.masterarbeit.epa.drawing.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.drawing.tree.TreeBuildingVisitor
import moritz.lindner.masterarbeit.epa.filter.PartitionFrequencyFilter
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.io.File
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
open class Benchmark {
    @Setup()
    fun printJvmArgs() {
        println(
            "JVM Args: " +
                java.lang.management.ManagementFactory
                    .getRuntimeMXBean()
                    .inputArguments,
        )
    }

    @Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
    @Fork(1)
    @Benchmark
    open fun epaConstruction() {
        val sample =
            File("/Users/moritzlindner/programming/Masterarbeit/epa-visualizer/eventlogs/sample.xes") to SampleEventMapper()
        val sample2 = File("./epa/src/main/resources/eventlogs/sample2.xes") to SampleEventMapper()
        val loops = File("./epa/src/main/resources/eventlogs/loops.xes") to SampleEventMapper()
        val challenge2017Offers =
            File("/Users/moritzlindner/programming/Masterarbeit/epa-visualizer/eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to
                BPI2017OfferChallengeEventMapper()
        val challenge2017 =
            File(".eventlogs/BPI Challenge 2017.xes.gz") to BPI2017ChallengeEventMapper()
        val challenge2018 = File("./epa/src/main/resources/eventlogs/BPI Challenge 2018.xes.gz") to BPI2018ChallangeMapper()

        val (file, mapper) = sample

        val epa =
            ExtendedPrefixAutomataBuilder<Long>()
                .setFile(file)
                .setEventLogMapper(mapper)
                .build()

        buildLayout(epa)

        val filter = PartitionFrequencyFilter<Long>(0.01f)
        val filteredEpa = filter.apply(epa)

        buildLayout(filteredEpa)
    }

    fun buildLayout(epa: ExtendedPrefixAutomata<Long>) {
        val walker = TreeBuildingVisitor<Long>()
        epa.copy().acceptDepthFirst(walker)
        val root = walker.root
        val layout =
            RadialWalkerTreeLayout(
                layerSpace = 10.0f,
                margin = 0.1f,
            )
        layout.build(root)
    }
}
