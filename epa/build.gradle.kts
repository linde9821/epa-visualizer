import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import java.net.InetAddress

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("moritz.lindner.masterarbeit.metrics.filter.CompleteFilterEvaluationKt")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation(libs.guava)
    implementation(libs.bundles.log4j)
    implementation(libs.logging)
    implementation(libs.jaxb.api)
    implementation(libs.openxes)
    implementation(libs.rtree)
    implementation(libs.csv)
    implementation(libs.coroutines.core)
    implementation(libs.jts.core)
    implementation(libs.bundles.serialization)
    implementation(libs.bundles.dl4j)
    implementation(libs.bundles.smile)

    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()

    if (osName.contains("mac") && osArch.contains("aarch64")) {
        implementation("org.nd4j:nd4j-native:1.0.0-M2.1:macosx-arm64")
        implementation("org.bytedeco:openblas:0.3.30-1.5.12:macosx-arm64")
    }

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.selfie.runner)
    testImplementation(libs.assertjCore)
}

tasks.withType<JavaExec> {
    val hostname = InetAddress.getLocalHost().hostName.lowercase()
    val isServer = hostname.contains("gruenau")

    if (isServer) {
        logger.info("Running on Server... Configuring for high performance run")
        maxHeapSize = "1024g"

        jvmArgs(
            "-Xms1024g",                   // Match Xmx to Xms to lock memory immediately
            "-Xss1m",
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=500",
            "-XX:+ExitOnOutOfMemoryError",
            "-XX:+AlwaysPreTouch",         // Still critical; will take a few mins to start up 1TB.
            "-XX:G1HeapRegionSize=32M",    // Mandatory for heaps >32GB to reduce region count.

            // --- NUMA Awareness: CRITICAL for AMD EPYC Dual-Socket ---
            "-XX:+UseNUMA",                // Optimizes memory access for EPYC's multi-die architecture

            // --- Parallelism Tuning for 256 Threads ---
            "-XX:ParallelGCThreads=128",   // Use 50% of cores for "Stop-the-World" phases
            "-XX:ConcGCThreads=32",        // Background concurrent marking threads

            // --- Throughput Optimizations ---
            "-XX:InitiatingHeapOccupancyPercent=45", // Start GC earlier to avoid "To-space exhausted"
            "-XX:+UnlockDiagnosticVMOptions",
//            "-XX:-DoEscapeAnalysis"        // Optional: Only use if you notice weird object allocation issues
        )
    }

    systemProperty("project.root", project.rootDir.absolutePath)
}

tasks.test {
    jvmArgs("-Xss4m")
    maxHeapSize = "2g"
    useJUnitPlatform()
    environment(properties.filter { it.key == "selfie" })
    inputs.files(
        fileTree("src/test") {
            include("**/*.ss")
        },
    )
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showExceptions = true
        exceptionFormat = FULL
    }
}