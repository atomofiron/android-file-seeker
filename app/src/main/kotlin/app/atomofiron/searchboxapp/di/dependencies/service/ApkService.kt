package app.atomofiron.searchboxapp.di.dependencies.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.core.net.toUri
import app.atomofiron.common.util.Android
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.model.explorer.NodeContent.AndroidApp
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.launch
import app.atomofiron.searchboxapp.utils.launchable
import app.atomofiron.searchboxapp.utils.toRslt
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ApkService(
    private val context: Context,
    private val installer: PackageInstaller,
) {
    fun install(content: AndroidApp, action: String): Rslt<Unit> = try {
        when {
            content.splitApk -> installApks(content, action)
            else -> installApk(content.ref, action, stringId = content.info?.stringId())
        }
    } catch (e: Exception) {
        e.toRslt()
    }

    private fun installApks(content: AndroidApp, action: String): Rslt<Unit> {
        val stringId = content.info?.stringId()
        return ZipInputStream(BufferedInputStream(content.ref.stream())).use { stream ->
            install(stream.available().toLong(), action) {
                var entry: ZipEntry? = stream.nextEntry
                    ?: return@install Rslt.Err("archive is empty")
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(Const.DOT_APK, ignoreCase = true)) {
                        val name = entry.name.let { name -> stringId?.let { it + name } ?: name }
                        openWrite(name, 0, entry.size).use { output ->
                            stream.copyTo(output)
                            fsync(output)
                        }
                    }
                    entry = stream.nextEntry
                }
                return@install Rslt.Ok
            }
        }
    }

    fun installApk(ref: NodeRef, action: String, stringId: String? = null, silently: Boolean = false): Rslt<Unit> {
        val stream = ref.stream()
        stream ?: return Rslt.Err("unknown error")
        val length = stream.available().toLong()
        return install(length, action, silently) {
            openWrite(stringId ?: "unused", 0, length).use { output ->
                stream.use {
                    it.copyTo(output)
                    fsync(output)
                }
            }
            return@install Rslt.Ok
        }
    }

    private fun install(
        length: Long,
        action: String,
        silently: Boolean = false,
        block: PackageInstaller.Session.() -> Rslt<Unit>,
    ) = try {
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        if (silently) {
            params.setAppPackageName(context.packageName)
            params.setSize(length)
            if (Android.S) params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            if (Android.T) params.setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
        }
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            session.block()
            val intent = Intents.installing(context)
            intent.action = action
            intent.setPackage(context.packageName)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, flags)
            try {
                session.commit(pendingIntent.intentSender)
                Rslt.Ok
            } catch (e: Exception) {
                e.toRslt()
            }
        }
    } catch (e: Exception) {
        e.toRslt()
    }

    fun launchable(packageName: String): Boolean = context.packageManager.launchable(packageName)

    fun launchApk(packageName: String) = context.launch(packageName)

    private fun NodeRef.stream() = when {
        isContent -> context.contentResolver.openInputStream(path.toUri())
        else -> FileInputStream(path)
    }
}