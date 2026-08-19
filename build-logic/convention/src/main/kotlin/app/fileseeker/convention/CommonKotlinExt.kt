package app.fileseeker.convention

import app.fileseeker.convention.AppConfig
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion

internal fun CommonExtension.configureKotlinAndroid() {
    buildToolsVersion = AppConfig.buildToolsVersion
    compileSdk {
        version = release(AppConfig.compileSdk) { minorApiLevel = AppConfig.compileSdkMinor }
    }

    defaultConfig.minSdk = AppConfig.minSdk
    compileOptions.sourceCompatibility = JavaVersion.VERSION_17
    compileOptions.targetCompatibility = JavaVersion.VERSION_17
}
