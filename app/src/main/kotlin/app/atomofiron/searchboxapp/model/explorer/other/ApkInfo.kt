package app.atomofiron.searchboxapp.model.explorer.other

data class ApkInfo(
    val icon: Thumbnail?,
    val appName: String,
    val versionName: String,
    val versionCode: Int,
    val packageName: String,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val compileSdkVersion: Int?,
    val signature: ApkSignature?,
) {
    fun stringId() = appName + versionCode
}
