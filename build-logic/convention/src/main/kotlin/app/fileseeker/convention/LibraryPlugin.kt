package app.fileseeker.convention

import app.fileseeker.convention.app.fileseeker.convention.configureKotlinAndroid
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
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
            buildConfigField("boolean", "DEBUG_BUILD", "false")
            buildConfigField("String", "PACKAGE_NAME", "\"${AppConfig.packageId}\"")
            buildConfigField("String", "AUTHORITY", "\"${AppConfig.fileProvider}\"")
            buildConfigField("debug.LeakWatcher", "leakWatcher", "null")
            manifestPlaceholders["PACKAGE_NAME"] = AppConfig.packageId
            manifestPlaceholders["PROVIDER"] = AppConfig.fileProvider
        }
        buildTypes {
            getByName("debug") {
                buildConfigField("boolean", "DEBUG_BUILD", "true")
                buildConfigField("String", "PACKAGE_NAME", "\"${AppConfig.packageIdDebug}\"")
                buildConfigField("String", "AUTHORITY", "\"${AppConfig.fileProviderDebug}\"")
                buildConfigField("debug.LeakWatcher", "leakWatcher", "new debug.LeakWatcherImpl()")
                manifestPlaceholders["PACKAGE_NAME"] = AppConfig.packageIdDebug
                manifestPlaceholders["PROVIDER"] = AppConfig.fileProviderDebug
            }
            create("alpha") {
            }
            create("beta") {
            }
            getByName("release") {
            }
        }
    }
}

abstract class AndroidLibraryPublishMetaData {
    var isPublishEnabled = true
    var publicationList: List<PublicationDetail> = emptyList()
}

data class PublicationDetail(
    val variantName: String,
    val groupId: String,
    val artifactId: String,
    val version: String,
    val name: String? = null,
    val description: String? = null
)

internal fun Project.configureAndroidLibraryPublish(metaData: AndroidLibraryPublishMetaData) {
    if (!metaData.isPublishEnabled) return

    afterEvaluate {
        val allVariants = (extensions.getByName("android") as LibraryExtension).libraryVariants.map { it.name }.toSet()
        val eligiblePublication = metaData.publicationList.filter { allVariants.contains(it.variantName) }

        val publishing = extensions.getByType(PublishingExtension::class.java) // from maven-publish plugin

        eligiblePublication.forEach { publication ->
            publishing.publications.create(publication.variantName, MavenPublication::class.java) {
                groupId = publication.groupId
                artifactId = publication.artifactId
                version = publication.version

                from(components.getAt(publication.variantName))

                pom {
                    name.set(publication.name)
                    description.set(publication.description)
                }
            }
        }
    }
}

