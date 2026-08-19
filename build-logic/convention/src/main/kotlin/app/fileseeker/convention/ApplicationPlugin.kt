package app.fileseeker.convention

import app.fileseeker.convention.configureKotlinAndroid
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("com.google.android.gms.oss-licenses-plugin")
            }
            extensions.configure<ApplicationExtension> {
                namespace = AppConfig.packageId
                configureKotlinAndroid()
                configureAndroidCommon()
            }
            afterEvaluate {
                registerUpdateBundledOssLicensesTask()
            }
        }
    }

    private fun ApplicationExtension.configureAndroidCommon() {
        buildFeatures {
            buildConfig = true
        }
        defaultConfig {
            applicationId = AppConfig.packageId
            targetSdk = AppConfig.targetSdk
            versionCode = AppConfig.versionCode
            versionName = AppConfig.versionName
        }
        androidResources {
            generateLocaleConfig = true
            //localeFilters += arrayOf("en", "ru", "sr", "b+sr+Latn")
        }
        buildTypes {
            getByName("debug") {
                applicationIdSuffix = ".debug"
                isMinifyEnabled = false
            }
            create("alpha") {
                applicationIdSuffix = ".debug"
                isDebuggable = true
                isMinifyEnabled = false
                signingConfig = signingConfigs.getByName("debug")
            }
            create("beta") {
                isDebuggable = false
                isMinifyEnabled = true
                signingConfig = signingConfigs.getByName("debug")
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../app/proguard-rules.pro")
            }
            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "../app/proguard-rules.pro")
            }
        }
    }

    private fun Project.registerUpdateBundledOssLicensesTask() {
        val releaseTaskName = "releaseOssLicensesTask"
        if (tasks.findByName(releaseTaskName) == null) {
            return logger.lifecycle("OSS release task not found, skipping OSS license bundling for $path")
        }
        tasks.register<Copy>("updateBundledLicenses") {
            group = "licenses"
            description = "Generate OSS licenses (release) and bundle them into src/main/assets"

            dependsOn(releaseTaskName)

            from(layout.buildDirectory.dir("generated/third_party_licenses/release"))
            val appProject = rootProject.project(":app")
            into(appProject.layout.projectDirectory.dir("src/main/assets/licenses"))
            doFirst {
                logger.lifecycle("Updating bundled OSS licenses for $path")
            }
        }
    }
}
