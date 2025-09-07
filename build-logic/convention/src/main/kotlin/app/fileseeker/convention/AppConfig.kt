package app.fileseeker.convention

// signature key alias atomofiron_2020

@Suppress("ConstPropertyName")
object AppConfig {

    const val packageId = "app.atomofiron.searchboxapp"
    const val fileProvider = "$packageId.FileProvider"
    const val externalProvider = "$packageId.ExternalProvider"

    const val packageIdDebug = "$packageId.debug"
    const val fileProviderDebug = "$packageIdDebug.FileProvider"
    const val externalProviderDebug = "$packageIdDebug.ExternalProvider"

    const val minSdk = 24
    const val targetSdk = 35
    const val compileSdk = 36
    const val buildToolsVersion = "36.0.0"

    const val versionCode = 25
    const val versionName = "1.4.6"
}