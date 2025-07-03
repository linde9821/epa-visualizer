package test

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.construction.builder.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.features.filter.PartitionFrequencyFilter
import moritz.lindner.masterarbeit.epa.features.layout.implementations.RadialWalkerTreeLayout
import moritz.lindner.masterarbeit.epa.features.layout.tree.EpaToTree
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
        val challenge2017Offers =
            File("./../data/eventlogs/BPI Challenge 2017 - Offer log.xes.gz") to
                BPI2017OfferChallengeEventMapper()

        val (file, mapper) = challenge2017Offers

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
        val walker = EpaToTree<Long>()
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
