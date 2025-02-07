package app.fileseeker.convention

@Suppress("ConstPropertyName")
object AppConfig {

    const val packageId = "app.atomofiron.searchboxapp"
    const val fileProvider = "$packageId.FileProvider"

    const val packageIdDebug = "$packageId.debug"
    const val fileProviderDebug = "$packageIdDebug.FileProvider"

    const val minSdk = 24
    const val targetSdk = 35
    const val compileSdk = 35
    const val buildToolsVersion = "35.0.0"

    const val versionCode = 18
    const val versionName = "1.4.2"
}