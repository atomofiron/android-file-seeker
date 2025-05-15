package app.atomofiron.searchboxapp.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.signature
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo

fun Context.apkInfo(path: String): ApkInfo? = packageManager.apkInfo(path)

fun PackageManager.apkInfo(path: String): ApkInfo? {
    val packageInfo = getPackageArchiveInfo(path, PackageManager.GET_SIGNATURES or PackageManager.GET_SIGNING_CERTIFICATES)
    val info = packageInfo?.applicationInfo
    info ?: return null
    info.sourceDir = path
    info.publicSourceDir = path
    packageInfo.installLocation
    return ApkInfo(
        appName = info.loadLabel(this).toString(),
        versionName = packageInfo.versionName.toString(),
        versionCode = PackageInfoCompat.getLongVersionCode(packageInfo).toInt(),
        packageName = packageInfo.packageName,
        minSdkVersion = info.minSdkVersion,
        targetSdkVersion = info.targetSdkVersion,
        compileSdkVersion = if (Android.S) info.compileSdkVersion else null,
        signature = packageInfo.signature(),
    )
}
