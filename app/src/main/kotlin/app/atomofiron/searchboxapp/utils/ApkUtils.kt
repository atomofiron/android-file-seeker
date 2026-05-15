package app.atomofiron.searchboxapp.utils

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.signature
import app.atomofiron.common.util.extension.then
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.di.dependencies.store.ApkInfoCache
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent.AndroidApp
import app.atomofiron.searchboxapp.model.explorer.NodeHash
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.utils.Const.DOT_APK
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

const val BASE_APK = "base$DOT_APK"
const val TEMP_APKS_DIR = "apks"
@Suppress("DEPRECATION")
private const val WITH_SIGNATURE = PackageManager.GET_SIGNATURES or PackageManager.GET_SIGNING_CERTIFICATES

fun PackageManager.apkInfo(path: String, icon: Boolean = true, signature: Boolean = false): ApkInfo? {
    val packageInfo = getPackageArchiveInfo(path, if (signature) WITH_SIGNATURE else 0)
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
        installLocation = packageInfo.installLocation,
        signature = packageInfo.signature(),
        withIcon = icon,
        withSignature = signature,
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
suspend fun AndroidApp.getAppContent(ref: NodeRef, asSu: Boolean, signature: Boolean = false): Rslt<AndroidApp> {
    val (hash, info) = ref.getCachedApkInfo(asSu = asSu, signature = signature)
    info?.let {
        return update(it).toOk()
    }
    return try {
        when {
            splitApk -> getApksContent(FileInputStream(ref.string), hash, signature = signature)
            else -> getApkContent(ref.string, hash, signature = signature)
        }
    } catch (e: Exception) {
        e.toRslt()
    }
}

suspend fun AndroidApp.getApksContent(input: InputStream?, hash: NodeHash? = null, signature: Boolean = false): Rslt<AndroidApp> {
    val tempDir = System.getProperty("java.io.tmpdir")
        ?: return Rslt.Err("No temp dir")
    val tmp = File("$tempDir/$TEMP_APKS_DIR/${Random.nextUInt()}")
    tmp.delete()
    tmp.parentFile
        ?.mkdir()
        ?.takeIf { tmp.createNewFile() }
        ?: return Rslt.Err("Can't create temp file")
    var containsMainApk = false
    try {
        ZipInputStream(BufferedInputStream(input)).use { stream ->
            var entry: ZipEntry? = stream.nextEntry
            while (entry != null) {
                if (entry.name.possibleMainApk()) {
                    FileOutputStream(tmp).use {
                        stream.copyTo(it)
                    }
                    containsMainApk = true
                    break
                }
                entry = stream.nextEntry
            }
        }
    } catch (e: Exception) {
        return e.toRslt()
    }
    return when {
        !containsMainApk -> Rslt.Err("Main .apk not found")
        tmp.length() == 0L -> Rslt.Err("Temp file is empty")
        else -> getApkContent(apkPath = tmp.absolutePath, hash, signature = signature)
    }.also { tmp.delete() }
}

private suspend fun AndroidApp.getApkContent(apkPath: String, hash: NodeHash?, signature: Boolean = false): Rslt<AndroidApp> {
    val manager = packageManager.value
        ?: return Rslt.Err("No package manager")
    val info = manager.apkInfo(apkPath, icon = true, signature = signature)
        ?: return Rslt.Err()
    hash?.let {
        ApkInfoCache.offer(it, withIcon = true, withSignature = signature, info)
    }
    return update(info).toOk()
}

private fun NodeRef.getCachedApkInfo(asSu: Boolean, signature: Boolean): Pair<NodeHash?, ApkInfo?> {
    val hash = NativeBridge.crcHash(this, asSu).ok()?.value
    return hash to hash
        ?.let { ApkInfoCache.get(it, withIcon = true, withSignature = signature) }
}

private fun AndroidApp.update(info: ApkInfo): AndroidApp = copy(info = info)

private fun String.possibleMainApk() = this == BASE_APK || !startsWith("config.") && endsWith(DOT_APK)
