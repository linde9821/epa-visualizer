plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)
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
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
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
