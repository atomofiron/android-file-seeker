package app.atomofiron.searchboxapp.utils

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.signature
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo

private const val flags = PackageManager.GET_SIGNATURES or PackageManager.GET_SIGNING_CERTIFICATES

fun PackageManager.apkInfo(path: String): ApkInfo? {
    val packageInfo = getPackageArchiveInfo(path, flags)
    val info = packageInfo?.applicationInfo
    info ?: return null
    info.sourceDir = path
    info.publicSourceDir = path
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

fun PackageManager.launchable(packageName: String): Boolean = getLaunchIntentForPackage(packageName) != null

fun Context.launch(packageName: String): Boolean {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
    launchIntent ?: return false.also {
        Toast.makeText(this, getString(R.string.unknown_error), Toast.LENGTH_LONG).show()
    }
    startActivity(launchIntent)
    return true
}
