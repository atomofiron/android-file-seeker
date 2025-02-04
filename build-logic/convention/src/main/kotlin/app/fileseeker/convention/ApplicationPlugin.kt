package app.fileseeker.convention

import app.fileseeker.convention.app.fileseeker.convention.configureKotlinAndroid
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }
            extensions.configure<BaseAppModuleExtension> {
                namespace = AppConfig.packageId
                configureKotlinAndroid()
                configureAndroidCommon()
            }
        }
    }

    private fun BaseAppModuleExtension.configureAndroidCommon() {
        buildFeatures {
            buildConfig = true
        }
        defaultConfig {
            applicationId = AppConfig.packageId
            targetSdk = AppConfig.targetSdk
            versionCode = AppConfig.versionCode
            versionName = AppConfig.versionName
        }
        buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
                applicationIdSuffix = ".debug"
            }
            create("alpha") {
                isDebuggable = true
                isMinifyEnabled = false
                signingConfig = signingConfigs.getByName("debug")
            }
            create("beta") {
                isDebuggable = false
                isMinifyEnabled = true
                signingConfig = signingConfigs.getByName("debug")
            }
            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }
    }
}
