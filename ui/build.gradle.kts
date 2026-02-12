import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.gradle.buildconfig.plugin)
    alias(libs.plugins.kotlin.serialization)
}

group = "moritz.lindner.masterarbeit"
version = "1.19.2"
val createMetrics = false

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
dependencies {
    implementation(compose.desktop.currentOs) {
        exclude(group = "org.jetbrains.compose.material")
    }
    implementation(libs.compose.resources)
    implementation(libs.jewel)
    implementation(libs.jewel.decorated)
    implementation(libs.intellijPlatform.icons)

    implementation(project(":epa"))

    implementation(libs.bundles.log4j)
    implementation(libs.logging)
    implementation(libs.jna.core)

    implementation(libs.bundles.filekit)
    implementation(libs.bundles.lets.plot)
    implementation(libs.bundles.serialization)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertjCore)
}

tasks.test {
    useJUnitPlatform()
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose-stability.conf"))

    if (createMetrics) {
        metricsDestination.set(layout.buildDirectory.dir("compose_metrics"))
        reportsDestination.set(layout.buildDirectory.dir("compose_reports"))
    }
}

compose.desktop {
    application {
        mainClass = "moritz.lindner.masterarbeit.ui.EPAVisualizerMainKt"

        javaHome =
            javaToolchains
                .launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(25))
                    vendor.set(JvmVendorSpec.JETBRAINS)
                }.get()
                .metadata.installationPath.asFile.absolutePath

        jvmArgs +=
            listOf(
                "-XX:MaxRAMPercentage=85",
                "-XX:+UseStringDeduplication",
                "-XX:+AlwaysPreTouch",
                "-XX:+UseG1GC",
                "--enable-native-access=ALL-UNNAMED"
            )

        buildTypes.release.proguard {
            isEnabled = false
            optimize = true
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "EPA Visualizer"
            description =
                "Interactive Visualization of Extended Prefix Automaton Using Radial Trees. A tool for analyzing trace variants in large, complex event logs using Extended Prefix Automata (EPA) and radial tidy tree layouts. This visualization approach encodes thousands of trace variants while minimizing visual clutter and supporting interactive filtering."
            packageVersion = version.toString()
            vendor = "Moritz Lindner"
            licenseFile = rootProject.file("LICENSE")

            macOS {
                iconFile.set(project.file("src/main/resources/logo.icns"))
                bundleID = "moritz.lindner.masterarbeit"
                dockName = "EPA Visualizer"
                modules(
                    "java.management",
                    "jdk.unsupported",
                    "jdk.security.auth",
                    "java.naming",
                )
            }

            windows {
                iconFile.set(project.file("src/main/resources/logo.ico"))
                menuGroup = "EPA Visualizer"
                modules(
                    "java.management",
                    "jdk.unsupported",
                    "jdk.security.auth",
                    "java.naming",
                )
            }

            linux {
                shortcut = true
                menuGroup = "EPA Visualizer"
                iconFile.set(project.file("src/main/resources/logo.png"))
                modules(
                    "java.management",
                    "jdk.unsupported",
                    "jdk.security.auth",
                    "java.naming",
                )
            }
        }
    }
}

fun getGitCommitHash(): String =
    try {
        providers
            .exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
            }.standardOutput.asText
            .get()
            .dropLast(1)
    } catch (_: Exception) {
        "unknown"
    }

buildConfig {
    val gitHash = getGitCommitHash()
    val versionWithHash = "${project.version}-$gitHash"
    logger.info("Version: $versionWithHash")
    buildConfigField("String", "APP_VERSION", "\"${versionWithHash}\"")
    packageName("moritz.lindner.masterarbeit.buildconfig")
}
