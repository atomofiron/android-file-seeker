package app.fileseeker.convention

import app.fileseeker.convention.app.fileseeker.convention.configureKotlinAndroid
import com.android.build.api.dsl.VariantDimension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.run {
            apply("com.android.library")
            apply("org.jetbrains.kotlin.android")
            apply("maven-publish")
        }
        project.extensions.configure<LibraryExtension> {
            configureKotlinAndroid()
            configureAndroidCommon()
        }
    }

    private fun LibraryExtension.configureAndroidCommon() {
        buildFeatures {
            viewBinding = true
            buildConfig = true
        }
        defaultConfig {
            resValue("string", "version_name", "v${AppConfig.versionName} (${AppConfig.versionCode})")
        }
        buildTypes {
            getByName("debug") {
                buildConfig(true)
                leakWatcher("new debug.LeakWatcherImpl()")
            }
            create("alpha") {
                buildConfig(true)
                leakWatcher()
            }
            create("beta") {
                buildConfig(false)
                leakWatcher()
            }
            getByName("release") {
                buildConfig(false)
                leakWatcher()
            }
        }
    }
}

fun VariantDimension.buildConfig(debug: Boolean) {
    val packageId = if (debug) AppConfig.packageIdDebug else AppConfig.packageId
    val fileProvider = if (debug) AppConfig.fileProviderDebug else AppConfig.fileProvider
    buildConfigField("boolean", "DEBUG_BUILD", debug.toString())
    buildConfigField("String", "PACKAGE_NAME", "\"$packageId\"")
    buildConfigField("String", "VERSION_NAME", "\"${AppConfig.versionName}\"")
    buildConfigField("String", "AUTHORITY", "\"$fileProvider\"")
    manifestPlaceholders["PACKAGE_NAME"] = packageId
    manifestPlaceholders["PROVIDER"] = fileProvider
}

fun VariantDimension.leakWatcher(value: String? = null) {
    buildConfigField("debug.LeakWatcher", "leakWatcher", value.toString())
}
