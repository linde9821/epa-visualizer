import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.gradle.buildconfig.plugin)
}

group = "moritz.lindner.masterarbeit"
version = "1.2.1"

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
    implementation(compose.desktop.currentOs)
    implementation(libs.bundles.log4j)
    implementation(libs.logging)
    implementation(compose.components.resources)

    implementation(libs.jewel)
    implementation(libs.jna.core)
    implementation(libs.jewel.decorated)
    implementation(libs.intellijPlatform.icons)

    implementation(project(":epa"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertjCore)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
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
                "--add-opens", "java.base/sun.misc=ALL-UNNAMED",
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.management/java.lang.management=ALL-UNNAMED"
            )

        buildTypes.release.proguard {
            isEnabled = false
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "EPA Visualizer"
            description = "EPA Visualizer"
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

fun getGitCommitHash(): String {
    return try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .directory(file("."))
            .start()
        val result = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        if (process.exitValue() == 0) result else "unknown"
    } catch (_: Exception) {
        "unknown"
    }
}

buildConfig {
    val gitHash = getGitCommitHash()
    val versionWithHash = "${project.version}-$gitHash"
    buildConfigField("String", "APP_VERSION", "\"${versionWithHash}\"")
    packageName("moritz.lindner.masterarbeit.buildconfig")
}
