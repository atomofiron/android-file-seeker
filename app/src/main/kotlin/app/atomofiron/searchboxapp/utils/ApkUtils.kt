package app.atomofiron.searchboxapp.utils

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo

fun Context.apkInfo(path: String): ApkInfo? {
    val packageInfo = packageManager.getPackageArchiveInfo(path, 0)
    val info = packageInfo?.applicationInfo
    info ?: return null
    info.sourceDir = path
    info.publicSourceDir = path
    return ApkInfo(
        appName = info.loadLabel(packageManager).toString(),
        versionName = packageInfo.versionName.toString(),
        versionCode = PackageInfoCompat.getLongVersionCode(packageInfo).toInt(),
        packageName = packageInfo.packageName,
    )
}
