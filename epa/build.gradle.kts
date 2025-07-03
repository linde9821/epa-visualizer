plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)
    id("me.champeau.jmh") version "0.7.3"
}

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/apromore/ApromoreCore_SupportLibs/master/mvn-repo/")
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.selfie.runner)
    testImplementation(libs.assertjCore)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.guava)
    implementation(libs.bundles.log4j)
    implementation(libs.logging)
    implementation(libs.jaxb.api)
    implementation(libs.openxes)
    implementation(libs.progressbar)
    implementation(libs.rtree)
    implementation(libs.csv)
    implementation("org.openjdk.jmh:jmh-core:1.37")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<me.champeau.jmh.JMHTask> {
    // Get from command line, fallback to default if not present
    jvmArgs = (project.findProperty("jmhJvmArgs") as String?)?.split(" ") ?: listOf()
    resultFormat = (project.findProperty("jmhResultFormat") as String?) ?: "CSV"
    resultsFile = file((project.findProperty("jmhResult") as String?) ?: "epa/build/jmh-result.csv")
}

tasks.test {
    useJUnitPlatform()
    environment(properties.filter { it.key == "selfie" }) // optional, see "Overwrite everything" below
    inputs.files(
        fileTree("src/test") {
            // optional, improves up-to-date checking
            include("**/*.ss")
        },
    )
}
