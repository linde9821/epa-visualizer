import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.kotlin.plugin.compose) apply false
    alias(libs.plugins.gradle.buildconfig.plugin) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

subprojects {
    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_25
            freeCompilerArgs.addAll(
                listOf(
                    "-Xinline-classes",
                ),
            )
        }
    }
}
