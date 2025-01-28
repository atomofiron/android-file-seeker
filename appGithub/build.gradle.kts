import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("app.fileseeker.convention.application")
}

android {
    defaultConfig {
        val threshold = Date().apply { time += 1000 * 60 * 60 * 8 }
        val date = SimpleDateFormat ("yyyy-MM-dd'T'hh:mm:ss'Z'").format(threshold)
        buildConfigField("String", "UPDATE_THRESHOLD", "\"$date\"")
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":app"))
    implementation(libs.ktor.negotiation)
    implementation(libs.ktor.core)
    implementation(libs.ktor.json)
    implementation(libs.ktor.cio)
}