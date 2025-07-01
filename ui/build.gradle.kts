import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.plugin.compose)
}

group = "moritz.lindner.masterarbeit"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://raw.githubusercontent.com/apromore/ApromoreCore_SupportLibs/master/mvn-repo/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.bundles.log4j)
    implementation(libs.logging)
    implementation(compose.materialIconsExtended)
    implementation(project(":epa"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

compose.desktop {
    application {
        mainClass = "moritz.lindner.masterarbeit.ui.EPAVisualizerMainKt"

        jvmArgs +=
            listOf(
                "-Xms2g",
                "-Xmx10g",
                "-XX:+UseZGC",
                "-XX:+UseStringDeduplication",
                "-XX:MaxGCPauseMillis=100",
                "-XX:+AlwaysPreTouch",
            )

        buildTypes.release.proguard {
            isEnabled = false
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "EPA Visualizer"
            packageVersion = "1.0.0"

            macOS {
                iconFile.set(project.file("src/main/resources/logo.icns"))
                bundleID = "moritz.lindner.masterarbeit"
            }

            windows {
                iconFile.set(project.file("src/main/resources/logo.ico"))
            }

            linux {
                iconFile.set(project.file("src/main/resources/logo.png"))
            }
        }
    }
}
