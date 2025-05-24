package app.atomofiron.searchboxapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.signature
import app.atomofiron.common.util.extension.then
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.NodeContent.File.AndroidApp
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.utils.ExplorerUtils.packageManager
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.random.Random
import kotlin.random.nextUInt

const val BASE_APK = "base.apk"
const val TEMP_APKS_DIR = "apks"
@Suppress("DEPRECATION") @SuppressLint("InlinedApi")
private const val flags = PackageManager.GET_SIGNATURES or PackageManager.GET_SIGNING_CERTIFICATES

fun PackageManager.apkInfo(path: String, icon: Boolean = true): ApkInfo? {
    val packageInfo = getPackageArchiveInfo(path, flags)
    val info = packageInfo?.applicationInfo
    info ?: return null
    info.sourceDir = path
    info.publicSourceDir = path
    return ApkInfo(
        icon = icon then { Thumbnail(info.loadIcon(this)) },
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

@Throws(IOException::class)
fun AndroidApp.getApksContent(zipPath: String): Rslt<AndroidApp> {
    return getApksContent(FileInputStream(zipPath))
}

@Throws(IOException::class)
fun AndroidApp.getApksContent(input: InputStream?): Rslt<AndroidApp> {
    val tempDir = System.getProperty("java.io.tmpdir")
        ?: return Rslt.Err("No temp dir")
    val tmp = File("$tempDir/$TEMP_APKS_DIR/${Random.nextUInt()}")
    tmp.delete()
    tmp.parentFile
        ?.mkdir()
        ?.takeIf { tmp.createNewFile() }
        ?: return Rslt.Err("Can't create temp file")
    ZipInputStream(BufferedInputStream(input)).use { stream ->
        var entry: ZipEntry? = stream.nextEntry
        while (entry != null) {
            if (entry.name == BASE_APK) {
                FileOutputStream(tmp).use {
                    stream.copyTo(it)
                }
                break
            }
            entry = stream.nextEntry
        }
    }
    if (tmp.length() == 0L) {
        return Rslt.Err("Temp file is empty")
    }
    return getApkContent(tmp.absolutePath).also {
        tmp.delete()
    }
}

fun AndroidApp.getApkContent(apkPath: String): Rslt<AndroidApp> {
    val packageManager = packageManager.value
        ?: return Rslt.Err("No package manager")
    val info = packageManager.apkInfo(apkPath)
    return copy(info = info).toRslt()
}
