import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    // Replace with the actual package and class name containing your `fun main()`
    mainClass.set("moritz.lindner.masterarbeit.metrics.CompleteFilterEvaluationKt")
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
    // Give it 64GB or even 128GB. With 1TB total, the server won't even feel it.
    maxHeapSize = "128g"

    jvmArgs(
        "-Xms32g",                     // Start with a large initial heap
        "-XX:+UseZGC",                 // Optimized for large memory/high core count
        "-XX:+ZGenerational",          // Best for Java 21+ (improves efficiency)
        "-XX:+ExitOnOutOfMemoryError",
        "-XX:+AlwaysPreTouch"          // Pre-allocates RAM at startup to avoid OS-level delays later
    )
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