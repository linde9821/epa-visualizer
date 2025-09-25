import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.kotlin.plugin.compose) apply false
    alias(libs.plugins.gradle.buildconfig.plugin) apply false
    alias(libs.plugins.composeHotReload) apply false
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
            freeCompilerArgs.addAll(
                listOf(
                    "-Xjvm-default=all",
                    "-Xinline-classes",
                ),
            )
        }
    }
}
