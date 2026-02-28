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
        logger.log(LogLevel.WARN, "Running on Server... Configuring for high performance run")
        maxHeapSize = "800g"

        jvmArgs(
            "-Xms800g",                    // Lock memory at 800GB immediately
            "-Xss1m",                      // 1MB is standard and safe here
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=250",    // Slightly tighter than 500ms to keep it snappy
            "-XX:+ExitOnOutOfMemoryError",
            "-XX:+AlwaysPreTouch",         // CRITICAL: Pre-allocating 800GB will take ~2-4 minutes at startup
            "-XX:G1HeapRegionSize=32M",    // Mandatory for heaps of this size

            // --- Intel Quad-Socket Optimization ---
            "-XX:+UseNUMA",                // Essential for 4-socket Intel systems
            "-XX:+UseCondCardMark",        // Reduces cache line contention on many-core Intel CPUs

            // --- GC Thread Scaling for 120 Threads ---
            "-XX:ParallelGCThreads=40",    // 1/3 of total threads is usually ideal for G1 on this gen
            "-XX:ConcGCThreads=10",        // Concurrent marking threads

            // --- Memory Efficiency ---
            "-XX:InitiatingHeapOccupancyPercent=35", // Start GC earlier to avoid fragmentation
            "-XX:+UseTransparentHugePages",
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