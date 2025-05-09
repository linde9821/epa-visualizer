rootProject.name = "epa-visualizer"
include("epa", "ui")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    plugins {
        // Define plugin versions here or rely on the version catalog
    }
}