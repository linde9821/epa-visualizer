plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/apromore/ApromoreCore_SupportLibs/master/mvn-repo/")
    maven("https://jitpack.io")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
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
    implementation(libs.bundles.serialization)
    implementation(libs.bundles.dl4j)
    implementation(libs.bundles.smile)
    implementation("org.locationtech.jts:jts-core:1.20.0")

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
