import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.gradle.buildconfig.plugin)
    alias(libs.plugins.composeHotReload)
}

group = "moritz.lindner.masterarbeit"
version = "1.11.0"

repositories {
    google()
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    maven("https://www.jetbrains.com/intellij-repository/releases/")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://raw.githubusercontent.com/apromore/ApromoreCore_SupportLibs/master/mvn-repo/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(compose.desktop.currentOs) {
        exclude(group = "org.jetbrains.compose.material")
    }
    implementation(compose.components.resources)
    implementation(libs.jewel)
    implementation(libs.jewel.decorated)
    implementation(libs.intellijPlatform.icons)

    implementation(project(":epa"))

    implementation(libs.bundles.log4j)
    implementation(libs.logging)
    implementation(libs.jna.core)

    implementation(libs.bundles.filekit)
    implementation(libs.bundles.lets.plot)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertjCore)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.JETBRAINS
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.JETBRAINS
    }
}

compose.desktop {
    application {
        mainClass = "moritz.lindner.masterarbeit.ui.EPAVisualizerMainKt"

        javaHome =
            javaToolchains
                .launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(21))
                    vendor.set(JvmVendorSpec.JETBRAINS)
                }.get()
                .metadata.installationPath.asFile.absolutePath

        jvmArgs +=
            listOf(
                "-Xmx14g",
                "-XX:+UseStringDeduplication",
                "-XX:+AlwaysPreTouch",
                "-XX:+UseG1GC",
            )

        buildTypes.release.proguard {
            isEnabled = false
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
                modules("jdk.unsupported")
            }

            windows {
                iconFile.set(project.file("src/main/resources/logo.ico"))
                menuGroup = "EPA Visualizer"
                modules("jdk.unsupported")
            }

            linux {
                menuGroup = "EPA Visualizer"
                iconFile.set(project.file("src/main/resources/logo.png"))
                modules("jdk.unsupported")
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
