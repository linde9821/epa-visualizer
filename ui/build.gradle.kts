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
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

compose.desktop {
    application {
        mainClass = "moritz.lindner.masterarbeit.ui.EPAVisualizerMainKt"

        jvmArgs +=
            listOf(
                "-Xms2g",
                "-Xmx18g",
                "-XX:+UseG1GC",
                "-XX:MaxGCPauseMillis=250",
                "-XX:+UseStringDeduplication",
                "-XX:+AlwaysPreTouch",
            )

        buildTypes.release.proguard {
            isEnabled = false
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "EPA Visualizer"
            description = "EPA Visualizer"
            packageVersion = "1.0.0"
            vendor = "Moritz Lindner"
            licenseFile = rootProject.file("LICENSE")

            macOS {
                iconFile.set(project.file("src/main/resources/logo.icns"))
                bundleID = "moritz.lindner.masterarbeit"
                dockName = "EPA Visualizer"
            }

            windows {
                iconFile.set(project.file("src/main/resources/logo.ico"))
                menuGroup = "EPA Visualizer"
            }

            linux {
                menuGroup = "EPA Visualizer"
                iconFile.set(project.file("src/main/resources/logo.png"))
            }
        }
    }
}
