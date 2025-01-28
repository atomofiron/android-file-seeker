// build-logic/convention/src/main/kotlin/AndroidConventionPlugin.kt
@file:Suppress("PackageDirectoryMismatch")

package app.fileseeker.convention.app.fileseeker.convention

import app.fileseeker.convention.AppConfig
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion

internal fun CommonExtension<*, *, *, *, *, *>.configureKotlinAndroid() {
    buildToolsVersion = AppConfig.buildToolsVersion
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        minSdk = AppConfig.minSdk
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
