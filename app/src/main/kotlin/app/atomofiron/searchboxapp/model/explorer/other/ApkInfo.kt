package app.atomofiron.searchboxapp.model.explorer.other

class ApkInfo(
    val appName: String,
    val versionName: String,
    val versionCode: Int,
    val packageName: String,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val compileSdkVersion: Int?,
    val signature: ApkSignature?,
)
