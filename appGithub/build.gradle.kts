import app.fileseeker.convention.AppConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("app.fileseeker.convention.application")
}

android {
    namespace = AppConfig.packageId

    defaultConfig {
        manifestPlaceholders["SFST"] = "specialUse"
        val threshold = Date().apply { time += 1000 * 60 * 60 * 8 }
        val date = SimpleDateFormat ("yyyy-MM-dd'T'hh:mm:ss'Z'").format(threshold)
        buildConfigField("String", "UPDATE_THRESHOLD", "\"$date\"")
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(project(":app"))
    implementation(libs.ktor.negotiation)
    implementation(libs.ktor.core)
    implementation(libs.ktor.json)
    implementation(libs.ktor.cio)
}