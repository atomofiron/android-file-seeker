import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("app.fileseeker.convention.application")
}

android {
    namespace = "app.atomofiron.searchboxapp"

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(project(":app"))
    implementation(libs.play.core)
    implementation(libs.play.core.ktx)
}